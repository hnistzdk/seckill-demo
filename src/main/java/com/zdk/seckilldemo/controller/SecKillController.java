package com.zdk.seckilldemo.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.zdk.seckilldemo.exception.GlobalException;
import com.zdk.seckilldemo.pojo.Order;
import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.rabbit.OrderMessageProducer;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.OrderService;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zdk
 * @date 2022/5/16 21:02
 */
@Api(value = "秒杀",tags = "秒杀")
@Slf4j
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

    @Autowired
    private DefaultRedisScript<Long> stockLuaScript;

    /**
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @ApiOperation(value = "v1秒杀接口-废弃")
    @PostMapping("/doSeckill1")
    public String doSeckill1(Model model, User user, Long goodsId){
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

    @ApiOperation(value = "v2秒杀接口-废弃")
    @PostMapping("/doSeckill2")
    @ResponseBody
    @SuppressWarnings("all")
    public ApiResp doSeckill2(User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        // TODO: 2022/5/18 优化1秒杀
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
        return ApiResp.success(orderDetailVo);
    }

    @ApiOperation(value = "v3秒杀接口-废弃")
    @PostMapping("/doSeckill3")
    @ResponseBody
    @SuppressWarnings("all")
    public ApiResp doSeckill3(User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        // TODO: 2022/5/18 优化2 内存标记防止库存为0仍访问redis
        //通过内存标记减少对redis的访问 如果内存标记库存直接为0 返回错误
        if (emptyStockMap.get(goodsId)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //查redis判断该用户是否已秒杀成功过
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() +":"+ goodsId);
        if (isOk(seckillOrder)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/19 2022/5/18 优化2 redis预减库存
        /*
         * 这里直接去减库存是有问题的,在减少的时候并没有去判断它的值是否还能减,
         * 这样假设这个用户的10个请求线程同时来到这个位置,就会直接将redis库存减到0,
         * 而后来的线程还会继续减为负数,而原来我们的处理是,减完后,判一下库存是否小于0了,小于0则加回去,
         * 这样虽然能够解决问题,但在难以保证在极端情况下还能正确,因为这整个操作并不原子性的,
         * 我们减redis库存也需要先比较是否能减,然后再减,所以要保证这两步操作的原子性 使用lua脚本
         */
        //进行预减库存
        Long stock = redisUtil.decr("seckillGoods:" + goodsId);
        //如果库存被减为负数了,把库存加回来 修改内存标记  返回库存不足
        if (stock<0){
            emptyStockMap.put(goodsId, true);
            redisUtil.incr("seckillGoods:" + goodsId);
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/18 异步下单
        //RabbitMQ异步下单
        orderMessageProducer.sendOderMessage(new SeckillOderMessage(user, goodsId));
        //返回正在排队的结果0给用户
        return ApiResp.success(0);
    }

    @ApiOperation(value = "v4秒杀接口-废弃")
    @PostMapping("/doSeckill4")
    @ResponseBody
    @SuppressWarnings("all")
    public ApiResp doSeckill4(User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        // TODO: 2022/5/18 优化2 内存标记防止库存为0仍访问redis
        //通过内存标记减少对redis的访问 如果内存标记库存直接为0 返回错误
        if (emptyStockMap.get(goodsId)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //查redis判断该用户是否已秒杀成功过
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() +":"+ goodsId);
        if (isOk(seckillOrder)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/19 优化3 lua脚本预减库存 解决redis库存负数问题
        //lua脚本进行预减库存
        Long stock = redisUtil.execute(stockLuaScript, "seckillGoods:" + goodsId, Collections.EMPTY_LIST);
        if (stock<0){
            emptyStockMap.put(goodsId, true);
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/18 异步下单
        //RabbitMQ异步下单
        orderMessageProducer.sendOderMessage(new SeckillOderMessage(user, goodsId));
        //返回正在排队的结果0给用户
        return ApiResp.success(0);
    }

    /**
     * 连的redis都是另一台虚拟机的
     * 1000个线程重复10次,执行3次,即三万线程
     * 10件秒杀商品
     * 8核8线程i7-9700的windows
     * 优化前QPS：1105.3/sec 发生超卖
     * 优化1：一开始不查商品,直接扣库存,扣库存使用stock_count = stock_count - 1并且判断当前stock_count>0
     *       订单插入增加user_id和goods_id的唯一索引防止超卖(仅在单体中不超卖 分布式系统不能解决超卖)
     *       同时在判断是否重复秒杀时，将秒杀订单存在redis，提高速度
     * 优化1后QPS：2382.1~2410.4/sec 没有超卖发生
     *
     * 优化2:
     *      1.使用redis进行预减库存(lua脚本)
     *      2.使用内存标记防止库存为0后仍较多线程访问redis
     *      3.使用RabbitMQ进行异步下单
     * 优化2后QPS：4943.2/sec 没有超卖发生
     *
     * @param user
     * @param goodsId
     * @return
     */
    @ApiOperation(value = "正式秒杀接口")
    @PostMapping("/{path}/doSeckill")
    @ResponseBody
    @SuppressWarnings("all")
    public ApiResp doSeckill(@PathVariable String path, User user, Long goodsId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        // TODO: 2022/5/19 优化4 秒杀地址隐藏+校验
        Boolean check = orderService.checkPath(user,goodsId,path);
        if (!check){
            return ApiResp.error(ApiRespEnum.REQUEST_ILLEGAL);
        }
        // TODO: 2022/5/18 优化2 内存标记防止库存为0仍访问redis
        //通过内存标记减少对redis的访问 如果内存标记库存直接为0 返回错误
        if (emptyStockMap.get(goodsId)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        //查redis判断该用户是否已秒杀成功过
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() +":"+ goodsId);
        if (isOk(seckillOrder)){
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/19 优化3 lua脚本预减库存 解决redis库存负数问题
        //lua脚本进行预减库存
        Long stock = redisUtil.execute(stockLuaScript, "seckillGoods:" + goodsId, Collections.EMPTY_LIST);
        if (stock<0){
            emptyStockMap.put(goodsId, true);
            return ApiResp.error(ApiRespEnum.REPEAT_ERROR);
        }
        // TODO: 2022/5/18 异步下单
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
    @ApiOperation(value = "查询秒杀结果")
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
    @ApiOperation(value = "获取秒杀地址")
    @GetMapping("/path")
    @ResponseBody
    public ApiResp path(User user,Long goodsId,String captcha){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        Boolean checkCaptcha = orderService.checkCaptcha(user, goodsId, captcha);
        if (!checkCaptcha){
            return ApiResp.error(ApiRespEnum.ERROR_CAPTCHA);
        }
        String path = orderService.createPath(user,goodsId);
        return ApiResp.success(path);
    }

    @ApiOperation(value = "获取验证码")
    @GetMapping("/captcha")
    public void captcha(User user,Long goodsId){
        if (user == null){
            throw new GlobalException(ApiRespEnum.SESSION_ERROR);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成算术验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130,32,3);
        //验证码存入redis 设置5分钟失效
        redisUtil.set("captcha:"+user.getId()+":"+goodsId,captcha.text(),300);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("用户:{} 验证码生成失败", user.getId());
        }
    }
}
