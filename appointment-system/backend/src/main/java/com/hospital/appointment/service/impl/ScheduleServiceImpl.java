package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.mapper.ScheduleMapper;
import com.hospital.appointment.model.vo.ScheduleDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl {

    private final ScheduleMapper scheduleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public List<ScheduleDetailVO> getAvailableSchedules(Long deptId) {
        LocalDate today = LocalDate.now();
        String cacheKey = RedisKeyConstants.DEPT_SCHEDULE_LIST + deptId + ":" + today.toString();

        // 1. 优先查缓存 (拦截 99% 的流量)
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.debug("命中 Redis 排班缓存: 科室[{}]", deptId);
            return (List<ScheduleDetailVO>) cachedData;
        }

        // 2. 缓存未命中，查数据库 (仅 1% 的流量透传)
        log.info("缓存未命中，回源拉取科室[{}]排班数据", deptId);
        List<ScheduleDetailVO> list = scheduleMapper.selectAvailableSchedulesByDept(deptId, today);

        // 3. 写入缓存，防止缓存雪崩设置 1 小时过期 (可加上随机数防雪崩)
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, list, 60, TimeUnit.MINUTES);
        }

        return list;
    }
}