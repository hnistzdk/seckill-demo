package com.zdk.seckilldemo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

//    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResp seckill(User user, GoodsVo goodsVo) {
        //扣库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goodsVo.getId()));
        Integer stockCount = seckillGoods.getStockCount();
//        if (stockCount>0){
            //库存够 扣库存
            seckillGoods.setStockCount(stockCount-1);
            seckillGoodsService.updateById(seckillGoods);
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
            orderMapper.insert(order);

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setUserId(user.getId());
            seckillOrder.setOrderId(order.getId());
            seckillOrder.setGoodsId(goodsVo.getId());
            seckillOrderService.save(seckillOrder);
            return ApiResp.success(order);
//        }else{
//            return ApiResp.error(ApiRespEnum.EMPTY_STOCK);
//        }
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
