package com.zdk.seckilldemo.service.serviceImpl;

import com.zdk.seckilldemo.mapper.OrderMapper;
import com.zdk.seckilldemo.pojo.Order;
import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.mapper.SeckillOrderMapper;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

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
