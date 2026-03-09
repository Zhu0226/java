package com.hospital.appointment.controller;

import com.hospital.appointment.common.Result;
import com.hospital.appointment.model.vo.ScheduleDetailVO;
import com.hospital.appointment.service.impl.ScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排班资源展示 API 接口 (高频读接口)
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleServiceImpl scheduleService;

    /**
     * 获取指定科室的排班列表
     * GET 请求，通过 @RequestParam 获取 URL 查询参数
     */
    @GetMapping("/list")
    public Result<List<ScheduleDetailVO>> listSchedules(@RequestParam("deptId") Long deptId) {

        // 基础防御性校验
        if (deptId == null || deptId <= 0) {
            return Result.error("非法的科室ID参数");
        }

        List<ScheduleDetailVO> data = scheduleService.getAvailableSchedules(deptId);
        return Result.success(data);
    }
}