package com.zdk.seckilldemo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zdk.seckilldemo.exception.GlobalException;
import com.zdk.seckilldemo.mapper.SeckillOrderMapper;
import com.zdk.seckilldemo.pojo.Order;
import com.zdk.seckilldemo.mapper.OrderMapper;
import com.zdk.seckilldemo.pojo.SeckillGoods;
import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdk.seckilldemo.service.SeckillGoodsService;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import com.zdk.seckilldemo.vo.GoodsVo;
import com.zdk.seckilldemo.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private GoodsService goodsService;

    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResp seckill(User user, GoodsVo goodsVo) {
        //一开始是先查一遍seckillGoods 然后更新时 set库存=seckillGoods.getStockCount-1;

        //优化： 不查，扣库存使用stock_count = stock_count - 1并且判断当前stock_count>0
        //扣库存-1
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));
        if (!result){
            return ApiResp.error(ApiRespEnum.ERROR);
        }
        //下单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goodsVo.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        int insert = orderMapper.insert(order);

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        boolean save = seckillOrderService.save(seckillOrder);
        if (insert>0&&save){
            //秒杀订单存入redis 加速判断重复秒杀
            redisUtil.set("seckillOrder:"+user.getId()+":"+goodsVo.getId(), seckillOrder);
        }
        return ApiResp.success(order);
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId == null){
            throw new GlobalException(ApiRespEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoById(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo(goodsVo, order);
        return orderDetailVo;
    }


}
