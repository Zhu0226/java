package com.hospital.appointment.config;

import com.hospital.appointment.common.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业级 RabbitMQ 架构配置
 * 包含：JSON 序列化器、死信队列(延迟队列)的绑定关系声明
 */
@Slf4j
@Configuration
public class RabbitConfig {

    /**
     * 替换默认的 JDK 序列化，使用 JSON 序列化消息体
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 1. 声明业务交换机 (Direct 模式)
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MqConstants.ORDER_EXCHANGE, true, false);
    }

    /**
     * 2. 声明【延迟队列】(无消费者，核心在于配置参数)
     * 企业级死信架构：设置消息的 TTL，并指定消息死亡后投递给谁
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MqConstants.ORDER_EXCHANGE);
        // 声明消息成为死信后，携带的全新路由键
        args.put("x-dead-letter-routing-key", MqConstants.ORDER_TIMEOUT_ROUTING_KEY);
        // 设置队列中消息的统一过期时间 (15分钟)
        args.put("x-message-ttl", MqConstants.ORDER_TTL);

        return QueueBuilder.durable(MqConstants.ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 3. 声明【超时处理队列】(消费者实际监听的队列)
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(MqConstants.ORDER_TIMEOUT_QUEUE, true);
    }

    /**
     * 4. 绑定：将【延迟队列】绑定到业务交换机
     */
    @Bean
    public Binding delayQueueBinding(Queue orderDelayQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderDelayQueue).to(orderExchange).with(MqConstants.ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 5. 绑定：将【超时处理队列】绑定到死信交换机 (这里复用了业务交换机)
     */
    @Bean
    public Binding timeoutQueueBinding(Queue orderTimeoutQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutQueue).to(orderExchange).with(MqConstants.ORDER_TIMEOUT_ROUTING_KEY);
    }
}