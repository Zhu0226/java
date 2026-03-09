package com.hospital.appointment.mq;

import com.hospital.appointment.common.MqConstants;
import com.hospital.appointment.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时死信消费者 (替代原有的定时任务扫描)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener {

    private final OrderServiceImpl orderService;

    /**
     * 监听死信队列
     * 消息会在下单 15 分钟后，准时、分秒不差地到达这里
     */
    @RabbitListener(queues = MqConstants.ORDER_TIMEOUT_QUEUE)
    public void processTimeoutOrder(String orderNo) {
        log.info("⏰ 收到订单超时死信消息，准备检验并取消订单，单号: [{}]", orderNo);
        try {
            // 调用 Service 层专门为 MQ 编写的取消逻辑
            orderService.processTimeoutCancelByMQ(orderNo);
        } catch (Exception e) {
            log.error(" MQ 消费异常：处理订单[{}]超时逻辑失败", orderNo, e);
            // 依赖 Spring AMQP 的重试机制，如果抛出异常，会自动重试 3 次
            throw e;
        }
    }
}