package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.MqConstants;
import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.mapper.OrderMapper;
import com.hospital.appointment.mapper.TicketPoolMapper;
import com.hospital.appointment.model.dto.OrderCancelDTO;
import com.hospital.appointment.model.dto.OrderCreateDTO;
import com.hospital.appointment.model.dto.PaymentCallbackDTO;
import com.hospital.appointment.model.entity.Order;
import com.hospital.appointment.model.enums.OrderStatusEnum;
import com.hospital.appointment.utils.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final TicketPoolMapper ticketPoolMapper;
    private final OrderMapper orderMapper;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    // 引入雪花算法发号器
    private final SnowflakeIdWorker snowflakeIdWorker;

    // ==========================================
    // 链路一：创建挂号订单
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public String createAppointmentOrder(OrderCreateDTO dto) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) throw new BusinessException("请重新登录");

        String lockKey = RedisKeyConstants.LOCK_ORDER_SUBMIT + currentUserId + ":" + dto.getScheduleId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BusinessException("系统处理中，请勿重复点击");
            }

            int existCount = orderMapper.countValidOrderByPatientAndSchedule(currentUserId, dto.getScheduleId());
            if (existCount > 0) {
                throw new BusinessException("您在该时段已有预约记录，请勿重复挂号");
            }

            String inventoryKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + dto.getScheduleId();
            Object ticketIdObj = redisTemplate.opsForList().leftPop(inventoryKey);

            if (ticketIdObj == null) {
                throw new BusinessException("抱歉，该排班号源已售罄");
            }
            Long ticketId = Long.valueOf(ticketIdObj.toString());

            try {
                int updatedRows = ticketPoolMapper.lockTicketById(ticketId);
                if (updatedRows == 0) {
                    throw new RuntimeException("数据库底层状态异常，无法锁定号源");
                }

                Order order = new Order();
                // 【核心变动】：赋予分布式环境绝对唯一的业务主键 ID，完全替代底层 MySQL 的自增 ID
                order.setId(snowflakeIdWorker.nextId());

                order.setOrderNo(generateOrderNo());
                order.setPatientId(currentUserId);
                order.setScheduleId(dto.getScheduleId());
                order.setTicketId(ticketId);
                order.setDoctorId(1001L);
                order.setAmount(new BigDecimal("50.00"));
                order.setPayStatus(0);
                order.setOrderStatus(OrderStatusEnum.UNPAID.getCode());

                orderMapper.insertOrder(order);
                log.info("🚀 秒杀分片下单成功！业务单号:[{}], 雪花ID:[{}], 分片依据患者:[{}]", order.getOrderNo(), order.getId(), currentUserId);

                // 【企业级修复】：只有在 MySQL 事务真正 Commit 之后，才向 RabbitMQ 发送消息。防幽灵消息。
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_DELAY_ROUTING_KEY, order.getOrderNo());
                        log.info("📨 事务已提交，单号[{}]已送入 MQ 延迟队列，15分钟后开启超时巡检", order.getOrderNo());
                    }
                });

                return order.getOrderNo();

            } catch (Exception e) {
                redisTemplate.opsForList().rightPush(inventoryKey, ticketIdObj.toString());
                throw e;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==========================================
    // 链路二：专供 MQ 调用的超时处理逻辑
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutCancelByMQ(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) return;

        if (order.getOrderStatus() != 0) {
            log.info("MQ 巡检结果：订单 [{}] 已非待支付状态，忽略处理", orderNo);
            return;
        }

        int updatedRows = orderMapper.cancelTimeoutOrder(order.getId(), order.getVersion());
        if (updatedRows == 0) {
            log.warn("MQ 巡检结果：订单 [{}] 状态已被其他线程抢占修改，忽略", orderNo);
            return;
        }

        int releasedRows = ticketPoolMapper.releaseTicket(order.getTicketId());
        if (releasedRows == 0) {
            throw new BusinessException("号源释放失败，触发数据一致性保护事务回滚");
        }

        // 【企业级修复】：防止 DB 事务回滚但缓存已更新。必须在 DB 确认提交后才还票给 Redis！
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String inventoryKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + order.getScheduleId();
                redisTemplate.opsForList().rightPush(inventoryKey, order.getTicketId().toString());
                log.info("💥 MQ 裁决完成：单号[{}], 号源[{}]已重新入池(DB+Redis)", order.getOrderNo(), order.getTicketId());
            }
        });
    }

    // ==========================================
    // 链路三：处理第三方支付成功回调
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public void processPaymentSuccess(PaymentCallbackDTO dto) {
        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) return;

        if (order.getOrderStatus() == 1 || order.getPayStatus() == 1) return;

        if (order.getOrderStatus() != 0) {
            log.error("严重对账异常：订单 [{}] 状态为 [{}], 收到延迟的支付成功流水[{}]！", order.getOrderNo(), order.getOrderStatus(), dto.getTradeNo());
            return;
        }

        int updated = orderMapper.updateOrderToPaid(order.getOrderNo(), order.getVersion());
        if (updated == 0) throw new BusinessException("系统繁忙，更新支付状态失败，请稍后重试");
        log.info("✅ 订单支付成功！单号: [{}], 第三方流水号: [{}]", dto.getOrderNo(), dto.getTradeNo());
    }

    // ==========================================
    // 链路四：患者主动取消订单
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public void cancelByPatient(OrderCancelDTO dto) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) throw new BusinessException("请重新登录");

        Order order = orderMapper.selectByOrderNoAndPatient(dto.getOrderNo(), currentUserId);
        if (order == null) throw new BusinessException("未找到可操作的订单");
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 1) {
            throw new BusinessException("当前订单状态不允许取消");
        }

        int updated = orderMapper.cancelOrderByPatient(order.getOrderNo(), order.getVersion());
        if (updated == 0) throw new BusinessException("订单状态已发生变更，请刷新页面后重试");

        int released = ticketPoolMapper.releaseTicket(order.getTicketId());
        if (released == 0) throw new BusinessException("号源释放失败，触发事务回滚");

        // 【企业级修复】：强一致性保障，DB 提交后才将号源推回 Redis
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String inventoryKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + order.getScheduleId();
                redisTemplate.opsForList().rightPush(inventoryKey, order.getTicketId().toString());
                log.info("🚫 患者主动取消订单成功！号源[{}]已重新入池(DB+Redis)", order.getTicketId());
            }
        });
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "GH" + dateStr + shortUuid;
    }
}