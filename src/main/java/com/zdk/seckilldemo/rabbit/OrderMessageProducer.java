package com.zdk.seckilldemo.rabbit;

import cn.hutool.json.JSONUtil;
import com.zdk.seckilldemo.config.RabbitConfig;
import com.zdk.seckilldemo.vo.SeckillOderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zdk
 * @date 2022/5/18 19:55
 * 异步下单生产者
 */
@Slf4j
@Component
public class OrderMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendOderMessage(SeckillOderMessage seckillOderMessage){
        log.debug("进入异步下单,下单信息为:{}",seckillOderMessage);
        rabbitTemplate.convertAndSend(RabbitConfig.SECKILL_ORDER_EXCHANGE_NAME, "seckill.oder", JSONUtil.toJsonStr(seckillOderMessage));
    }
}
