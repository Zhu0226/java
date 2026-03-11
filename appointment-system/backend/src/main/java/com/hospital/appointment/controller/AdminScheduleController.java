package com.hospital.appointment.controller;

import com.hospital.appointment.annotation.AuditLog;
import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.service.impl.AdminScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final AdminScheduleServiceImpl adminScheduleService;

    @AuditLog(module = "排班管理", action = "手动触发全院智能排班跑批引擎")
    @PostMapping("/trigger-generate")
    public Result<Void> triggerGeneration(@RequestParam("dateStr") String dateStr) {
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：非管理员禁止触发排班引擎");
        }

        try {
            LocalDate targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            adminScheduleService.autoGenerateAllDoctorsSchedules(targetDate);
            return Result.success();
        } catch (Exception e) {
            // 【核心修复3】：把隐藏的报错在控制台大声喊出来！
            log.error("💥 触发排班跑批失败！原因: ", e);
            return Result.error("生成失败，请联系开发人员！");
        }
    }
}