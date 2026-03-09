package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.mapper.AdminScheduleMapper;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
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

        // 1. 批量落库
        adminScheduleMapper.batchInsertTicketPool(ticketPools);

        // 2. 【高并发核心】：预热号源库存到 Redis (List 结构)
        String redisKey = RedisKeyConstants.SCHEDULE_TICKET_POOL + schedule.getId();
        for (TicketPool ticket : ticketPools) {
            // 将每一个具体的号源 ID 放入 Redis 队列
            redisTemplate.opsForList().rightPush(redisKey, ticket.getId().toString());
        }
        // 设置 Redis key 过期时间：比排班日期多保留1天，防止占用内存
        redisTemplate.expire(redisKey, 8, TimeUnit.DAYS);

        // 3. 淘汰该科室旧的查询缓存，确保前端能马上查到新数据
        String cacheKey = RedisKeyConstants.DEPT_SCHEDULE_LIST + deptId + ":" + LocalDate.now();
        redisTemplate.delete(cacheKey);

        log.info("🎯 排班自动生成并预热至Redis成功：医生[{}], 共 [{}] 个号源", doctorId, totalNum);
    }

    public void autoGenerateAllDoctorsSchedules(LocalDate targetDate) {
        log.info("====== 启动全院智能排班跑批引擎，目标日期: {} ======", targetDate);
        List<Map<String, Object>> doctors = adminScheduleMapper.selectAllActiveDoctors();
        for (Map<String, Object> doc : doctors) {
            Long doctorId = Long.valueOf(doc.get("doctorId").toString());
            Long deptId = Long.valueOf(doc.get("deptId").toString());
            try {
                generateDoctorSchedule(doctorId, deptId, targetDate, 1, LocalTime.of(8, 0), LocalTime.of(12, 0), 15);
                generateDoctorSchedule(doctorId, deptId, targetDate, 2, LocalTime.of(14, 0), LocalTime.of(18, 0), 15);
            } catch (Exception e) {
                log.error("自动化生成排班发生异常，医生ID: {}", doctorId, e);
            }
        }
        log.info("====== 全院排班跑批完毕 ======");
    }
}