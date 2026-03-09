package com.hospital.appointment.common;

/**
 * 消息队列常量字典
 * 规范：集中管理所有 Exchange, Queue, RoutingKey
 */
public class MqConstants {

    // ================= 订单超时死信架构 =================

    // 1. 业务交换机 (普通)
    public static final String ORDER_EXCHANGE = "hospital.order.exchange";

    // 2. 延迟队列 (存放刚下单的消息，不设置消费者，让其自然过期)
    public static final String ORDER_DELAY_QUEUE = "hospital.order.delay.queue";
    // 延迟队列的路由键
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    // 3. 死信交换机 (当消息在延迟队列过期后，会自动投递到这个交换机)
    // 为了简化，死信交换机我们复用上面的 ORDER_EXCHANGE，只需改变 RoutingKey 即可

    // 4. 超时处理队列 (真正被消费者监听的队列)
    public static final String ORDER_TIMEOUT_QUEUE = "hospital.order.timeout.queue";
    // 超时进入死信的路由键
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    // 订单超时时间：15分钟 (单位：毫秒)
    public static final long ORDER_TTL = 15 * 60 * 1000;
}