package com.zdk.seckilldemo.service.serviceImpl;

import com.zdk.seckilldemo.pojo.Order;
import com.zdk.seckilldemo.mapper.OrderMapper;
import com.zdk.seckilldemo.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
