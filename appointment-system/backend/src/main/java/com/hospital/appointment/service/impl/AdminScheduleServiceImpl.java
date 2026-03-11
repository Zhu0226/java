package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.mapper.AdminScheduleMapper;
import com.hospital.appointment.mapper.TicketPoolMapper;
import com.hospital.appointment.model.entity.Schedule;
import com.hospital.appointment.model.entity.TicketPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminScheduleServiceImpl {

    private final AdminScheduleMapper adminScheduleMapper;
    // 【核心修复1】：注入号源查询 Mapper
    private final TicketPoolMapper ticketPoolMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 【核心修复2】：去掉此处的事务注解，防止同类内部调用导致 AOP 失效
    public void generateDoctorSchedule(Long doctorId, Long deptId, LocalDate workDate,
                                       Integer shiftType, LocalTime startTime, LocalTime endTime, int intervalMinutes) {

        if (adminScheduleMapper.checkScheduleExists(doctorId, workDate, shiftType) > 0) {
            return;
        }

        List<TicketPool> ticketPools = new ArrayList<>();
        LocalTime currentSlotStart = startTime;
        int ticketNo = 1;

        while (currentSlotStart.plusMinutes(intervalMinutes).isBefore(endTime) || currentSlotStart.plusMinutes(intervalMinutes).equals(endTime)) {
            TicketPool ticket = new TicketPool();
            ticket.setTicketNo(ticketNo++);
            ticket.setStartTime(currentSlotStart);
            ticket.setEndTime(currentSlotStart.plusMinutes(intervalMinutes));
            ticketPools.add(ticket);
            currentSlotStart = currentSlotStart.plusMinutes(intervalMinutes);
        }

        int totalNum = ticketPools.size();
        if (totalNum == 0) return;

        Schedule schedule = new Schedule();
        schedule.setDoctorId(doctorId);
        schedule.setDeptId(deptId);
        schedule.setWorkDate(workDate);
        schedule.setShiftType(shiftType);
        schedule.setTotalNum(totalNum);
        schedule.setAvailableNum(totalNum);

        adminScheduleMapper.insertSchedule(schedule);

        for (TicketPool ticket : ticketPools) {
            ticket.setScheduleId(schedule.getId());
        }

        // 1. 批量落库 (此时对象内没有 ID)
        adminScheduleMapper.batchInsertTicketPool(ticketPools);

        // 2. 【核心修复3】：绝对可靠方案！从数据库重新查出刚才插入的号源，确保拿到真实的自增ID
        List<TicketPool> savedTickets = ticketPoolMapper.selectAvailableTickets(schedule.getId());

        // 3. 【高并发核心】：预热带有真实 ID 的号源库存到 Redis
        String redisKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + schedule.getId();
        for (TicketPool ticket : savedTickets) {
            // 现在 ticket.getId() 绝对不为 null 了！
            redisTemplate.opsForList().rightPush(redisKey, ticket.getId().toString());
        }

        redisTemplate.expire(redisKey, 8, TimeUnit.DAYS);

        // 4. 淘汰该科室旧的查询缓存，确保前端能马上查到新数据
        String cacheKey = RedisKeyConstants.DEPT_SCHEDULE_LIST + deptId + ":" + LocalDate.now();
        redisTemplate.delete(cacheKey);

        log.info("🎯 排班自动生成并预热至Redis成功：医生[{}], 共 [{}] 个号源", doctorId, totalNum);
    }

    // 【核心修复4】：将事务注解加在对外暴露的入口方法上，确保异常时 MySQL 整体回滚
    @Transactional(rollbackFor = Exception.class)
    public void autoGenerateAllDoctorsSchedules(LocalDate targetDate) {
        log.info("====== 启动全院智能排班跑批引擎，目标日期: {} ======", targetDate);
        List<Map<String, Object>> doctors = adminScheduleMapper.selectAllActiveDoctors();
        for (Map<String, Object> doc : doctors) {
            Long doctorId = Long.valueOf(doc.get("doctorId").toString());
            Long deptId = Long.valueOf(doc.get("deptId").toString());
            // 如果内部抛出异常，整个事务会回滚，保证 DB 和 Redis 的绝对一致性！
            generateDoctorSchedule(doctorId, deptId, targetDate, 1, LocalTime.of(8, 0), LocalTime.of(12, 0), 15);
            generateDoctorSchedule(doctorId, deptId, targetDate, 2, LocalTime.of(14, 0), LocalTime.of(18, 0), 15);
        }
        log.info("====== 全院排班跑批完毕 ======");
    }
}