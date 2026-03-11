package com.hospital.appointment.controller;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.vo.DashboardStatsVO;
import com.hospital.appointment.service.impl.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        // 校验必须是管理员才能看大盘
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅超级管理员可访问");
        }
        return Result.success(dashboardService.getGlobalStats());
    }
}