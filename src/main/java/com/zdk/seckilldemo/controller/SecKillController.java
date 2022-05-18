package com.zdk.seckilldemo.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdk.seckilldemo.pojo.Order;
import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.rabbit.OrderMessageProducer;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.OrderService;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import com.zdk.seckilldemo.vo.GoodsVo;
import com.zdk.seckilldemo.vo.SeckillOderMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zdk
 * @date 2022/5/16 21:02
 */
@Api(value = "秒杀",tags = "秒杀")
@Controller
@RequestMapping("/seckill")
public class SecKillController extends BaseController implements InitializingBean {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrderMessageProducer orderMessageProducer;

    private final Map<Long,Boolean> emptyStockMap = new ConcurrentHashMap<>();

    /**
     * 连的redis都是另一台虚拟机的
     * 1000个线程重复10次,执行3次,即三万线程
     * 8核8线程i7-9700的windows
     * 优化前QPS：65.1/sec 发生超卖
     * 优化后QPS：/sec
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @ApiOperation(value = "秒杀接口")
    @PostMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){
        // TODO: 2022/5/18 老版秒杀,非前后端分离
        if (user == null){
            return "login";
        }
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        //判断库存
        if (goodsVo.getStockCount()<1){
            model.addAttribute("errmsg", ApiRespEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断该用户是否已秒杀成功过
        SeckillOrder seckillOrder = seckillOrderService.getOne(
                new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null){
            model.addAttribute("errmsg", ApiRespEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        //进行秒杀
        ApiResp result = orderService.seckill(user, goodsVo);
        if (result.isFail(result)){
            model.addAttribute("errmsg", ApiRespEnum.SESSION_ERROR);
            return "secKillFail";
        }
        model.addAttribute("goods", goodsVo);
        model.addAttribute("order", result.getObject());
        return "orderDetail";
    }

    /**
     * 连的redis都是另一台虚拟机的
     * 1000个线程重复10次,执行3次,即三万线程
     * 8核8线程i7-9700的windows
     * 优化前QPS：1105.3/sec 发生超卖
     * 优化1：一开始不查商品,直接扣库存,扣库存使用stock_count = stock_count - 1并且判断当前stock_count>0
     *       订单插入增加user_id和goods_id的唯一索引防止超卖(仅在单体中不超卖 分布式系统不能解决超卖)
     *       同时在判断是否重复秒杀时，将秒杀订单存在redis，提高速度
     * 优化1后QPS：2382.1~2410.4/sec 没有超卖发生
     * @param user
     * @param goodsId
     * @return
     */
    @ApiOperation(value = "秒杀接口")
    @PostMapping("/doSeckill2")
    @ResponseBody
    @SuppressWarnings("all")
    public ApiResp doSeckill2(User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        // TODO: 2022/5/18 优化1秒杀
        /*
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        //先判一次库存是否大于0
        if (goodsVo.getStockCount()<1){
            return ApiResp.error(ApiRespEnum.EMPTY_STOCK);
        }
        //redis判断该用户是否已秒杀成功过
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() + goodsVo.getId());

        if (isOk(seckillOrder)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //进行秒杀
        ApiResp result = orderService.seckill(user, goodsVo);
        if (result.isFail(result)){
            return ApiResp.error(ApiRespEnum.ERROR);
        }
        OrderDetailVo orderDetailVo = new OrderDetailVo().setOrder((Order) result.getObject()).setGoods(goodsVo);
        return ApiResp.success(orderDetailVo);*/

        // TODO: 2022/5/18 优化2 redis预减库存
        //通过内存标记减少对redis的访问 如果内存标记库存直接为0 返回错误
        if (emptyStockMap.get(goodsId)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //查redis判断该用户是否已秒杀成功过
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() +":"+ goodsId);
        if (isOk(seckillOrder)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //进行预减库存
        Long stock = redisUtil.decr("seckillGoods:" + goodsId);
        //如果库存被减为负数了,把库存加回来 修改内存标记  返回库存不足
        if (stock<0){
            emptyStockMap.put(goodsId, true);
            redisUtil.incr("seckillGoods:" + goodsId);
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //RabbitMQ异步下单
        orderMessageProducer.sendOderMessage(new SeckillOderMessage(user, goodsId));
        //返回正在排队的结果0给用户
        return ApiResp.success(0);
    }

    /**
     * 在Bean属性注入后 将商品的库存加载到redis中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        if (!goodsVo.isEmpty()){
            for (GoodsVo vo : goodsVo) {
                redisUtil.set("seckillGoods:"+vo.getId(), vo.getStockCount());
                //内存标记  防止库存为0但大量请求仍去查询redis获取库存
                //这里使用本地内存标记库存的是否为0
                emptyStockMap.put(vo.getId(), false);
            }
        }
    }

    /**
     * 查询秒杀结果
     * @param user
     * @param goodsId
     * @return orderId:成功 -1:秒杀失败  0:排队中
     */
    @GetMapping("/getResult")
    @ResponseBody
    public ApiResp getResult (User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        //redis判断是否秒杀成功
        String seckillOrderStr = redisUtil.get("seckillOrder:" + user.getId() + ":" + goodsId);
        //如果已有订单 秒杀成功 返回订单id
        if (isOk(seckillOrderStr)){
            SeckillOrder seckillOrder = JSONUtil.toBean(seckillOrderStr, SeckillOrder.class);
            return ApiResp.success(seckillOrder.getOrderId());
        }else {
            //如果没有订单 说明用户可能会成功 可能不能成功
            //再判库存 如果库存没了 不可能成功 返回-1
            //如果库存还有 可能还在排队 返回0
            if (Integer.parseInt(redisUtil.get("seckillGoods:" + goodsId))<1){
                return ApiResp.error(ApiRespEnum.EMPTY_STOCK);
            }else {
                return ApiResp.success(0);
            }
        }
    }
}
