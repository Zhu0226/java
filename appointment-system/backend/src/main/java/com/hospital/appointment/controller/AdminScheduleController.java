package com.hospital.appointment.controller;

import com.hospital.appointment.annotation.AuditLog;
import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.service.impl.AdminScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final AdminScheduleServiceImpl adminScheduleService;

    /**
     * 管理员手动触发指定日期的全院排班生成
     * 【企业级改造】：加入审计日志留痕，任何人生成排班都会被记录！
     */
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
            return Result.error("日期格式错误或生成失败，请使用 yyyy-MM-dd 格式");
        }
    }
}