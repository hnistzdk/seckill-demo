package com.zdk.seckilldemo.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zdk
 * @date 2022/5/18 19:49
 * RabbitMQ配置类
 */
@Configuration
public class RabbitConfig {
    public static final String SECKILL_ORDER_EXCHANGE_NAME = "seckill.order.exchange";
    public static final String SECKILL_ORDER_QUEUE_NAME = "seckill.order.queue";

    /**
     * 秒杀订单交换机
     * @return
     */
    @Bean
    public TopicExchange seckillOrderExchange(){
        return ExchangeBuilder.topicExchange(SECKILL_ORDER_EXCHANGE_NAME)
                .build();
    }

    /**
     * 秒杀订单队列
     * @return
     */
    @Bean
    public Queue seckillOrderQueue(){
        return QueueBuilder.durable(SECKILL_ORDER_QUEUE_NAME)
                .build();
    }

    @Bean
    public Binding bindingSeckillOrder(
            @Qualifier("seckillOrderExchange") TopicExchange seckillOrderExchange,
            @Qualifier("seckillOrderQueue") Queue seckillOrderQueue
    ){
        return BindingBuilder.bind(seckillOrderQueue)
                .to(seckillOrderExchange)
                .with("seckill.#");
    }
}
