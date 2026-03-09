package com.hospital.appointment.task;

import com.hospital.appointment.service.impl.AdminScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 医院“滚动放号”定时任务调度中心
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleAutoGenerateTask {

    private final AdminScheduleServiceImpl adminScheduleService;

    /**
     * 行业经典玩法：每天凌晨 2 点执行，自动生成第 7 天的号源。
     * 这样前端永远能看到未来 7 天的号，且实现了平滑的“滚动放号”。
     * cron = "0 0 2 * * ?"
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeRollingScheduleGeneration() {
        log.info("触发滚动排班生成任务...");

        // 目标放号日期：当前日期往后推 7 天
        LocalDate targetDate = LocalDate.now().plusDays(7);

        adminScheduleService.autoGenerateAllDoctorsSchedules(targetDate);
    }
}