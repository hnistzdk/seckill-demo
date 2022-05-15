package com.zdk.seckilldemo.service.serviceImpl;

import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.mapper.SeckillOrderMapper;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀订单表 服务实现类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

}
