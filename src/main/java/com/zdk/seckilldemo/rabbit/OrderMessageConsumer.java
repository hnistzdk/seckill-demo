package com.zdk.seckilldemo.rabbit;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.zdk.seckilldemo.config.RabbitConfig;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.OrderService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.GoodsVo;
import com.zdk.seckilldemo.vo.SeckillOderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zdk
 * @date 2022/5/18 19:55
 * 异步下单消费者
 */
@Slf4j
@Component
public class OrderMessageConsumer {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisUtil redisUtil;

    @RabbitListener(queues = RabbitConfig.SECKILL_ORDER_QUEUE_NAME)
    public void receive(Message message, Channel channel){
        String msg = new String(message.getBody());
        SeckillOderMessage seckillOderMessage = JSONUtil.toBean(msg, SeckillOderMessage.class);
        User user = seckillOderMessage.getUser();
        Long goodsId = seckillOderMessage.getGoodsId();
        //真正下单之前判断数据库库存是否够
        GoodsVo goods = goodsService.findGoodsVoById(goodsId);
        if (goods.getStockCount()<1){
            return;
        }
        //再判一下是否重复秒杀
        String seckillOrder = redisUtil.get("seckillOrder:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isBlank(seckillOrder)){
            return;
        }
        //进行下单操作
        orderService.seckill(user,goods);
    }
}
