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
import com.hospital.appointment.model.vo.PatientOrderVO;
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
import java.util.List;
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

    private final SnowflakeIdWorker snowflakeIdWorker;

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

                // 【核心修复4】：从数据库动态查询真实的医生ID，去掉写死的 888L
                Long realDoctorId = orderMapper.selectDoctorIdByScheduleId(dto.getScheduleId());
                if (realDoctorId == null) {
                    throw new BusinessException("数据异常：该排班未找到对应的医生");
                }

                Order order = new Order();
                order.setId(snowflakeIdWorker.nextId());
                order.setOrderNo(generateOrderNo());
                order.setPatientId(currentUserId);
                order.setScheduleId(dto.getScheduleId());
                order.setTicketId(ticketId);

                order.setDoctorId(realDoctorId); // <-- 真实医生入库！

                order.setAmount(new BigDecimal("50.00"));
                order.setPayStatus(0);
                order.setOrderStatus(OrderStatusEnum.UNPAID.getCode());

                orderMapper.insertOrder(order);
                log.info("🚀 秒杀分片下单成功！业务单号:[{}], 雪花ID:[{}], 分片依据患者:[{}]", order.getOrderNo(), order.getId(), currentUserId);

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

    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutCancelByMQ(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) return;

        if (order.getOrderStatus() != 0) return;

        int updatedRows = orderMapper.cancelTimeoutOrder(order.getId(), order.getVersion());
        if (updatedRows == 0) return;

        int releasedRows = ticketPoolMapper.releaseTicket(order.getTicketId());
        if (releasedRows == 0) throw new BusinessException("号源释放失败，触发数据一致性保护事务回滚");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String inventoryKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + order.getScheduleId();
                redisTemplate.opsForList().rightPush(inventoryKey, order.getTicketId().toString());
                log.info("💥 MQ 裁决完成：单号[{}], 号源[{}]已重新入池(DB+Redis)", order.getOrderNo(), order.getTicketId());
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPaymentSuccess(PaymentCallbackDTO dto) {
        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null || order.getOrderStatus() == 1 || order.getPayStatus() == 1) return;

        int updated = orderMapper.updateOrderToPaid(order.getOrderNo(), order.getVersion());
        if (updated == 0) throw new BusinessException("系统繁忙，更新支付状态失败，请稍后重试");
        log.info("✅ 订单支付成功！单号: [{}], 第三方流水号: [{}]", dto.getOrderNo(), dto.getTradeNo());
    }

    public List<PatientOrderVO> getPatientOrders(Long patientId) {
        log.info("🔍 患者[{}]正在查询订单记录", patientId);
        return orderMapper.selectPatientOrders(patientId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) throw new BusinessException("请重新登录");

        Order order = orderMapper.selectByOrderNoAndPatient(orderNo, currentUserId);
        if (order == null) throw new BusinessException("未找到可操作的订单");
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 1) {
            throw new BusinessException("当前订单状态不允许取消");
        }

        int updated = orderMapper.cancelOrderByPatient(order.getOrderNo(), order.getVersion());
        if (updated == 0) throw new BusinessException("订单状态已发生变更，请刷新页面后重试");

        int released = ticketPoolMapper.releaseTicket(order.getTicketId());
        if (released == 0) throw new BusinessException("号源释放失败，触发事务回滚");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String inventoryKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + order.getScheduleId();
                redisTemplate.opsForList().rightPush(inventoryKey, order.getTicketId().toString());
                log.info("🚫 患者主动取消订单成功！号源[{}]已重新入池", order.getTicketId());
            }
        });
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "GH" + dateStr + shortUuid;
    }
}