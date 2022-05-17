package com.zdk.seckilldemo.service;

import com.zdk.seckilldemo.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.GoodsVo;
import com.zdk.seckilldemo.vo.OrderDetailVo;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
public interface OrderService extends IService<Order> {
    /**
     * 进行秒杀
     * @param user
     * @param goodsVo
     * @return
     */
    ApiResp seckill(User user, GoodsVo goodsVo);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);
}
