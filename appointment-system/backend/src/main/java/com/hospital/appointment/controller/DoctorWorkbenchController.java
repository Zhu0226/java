package com.hospital.appointment.controller;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.dto.ConsultationCompleteDTO;
import com.hospital.appointment.model.entity.Schedule;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import com.hospital.appointment.service.impl.DoctorWorkbenchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor/workbench")
@RequiredArgsConstructor
public class DoctorWorkbenchController {

    private final DoctorWorkbenchServiceImpl workbenchService;

    @GetMapping("/today-list")
    public Result<List<DoctorOrderVO>> getTodayList() {
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可访问工作台");
        }
        Long doctorId = UserContext.getUserId();
        if (doctorId == null || doctorId <= 0) return Result.error("医生登录状态异常");
        return Result.success(workbenchService.getTodayOrderList(doctorId));
    }

    @PostMapping("/complete")
    public Result<Void> completeConsultation(@Validated @RequestBody ConsultationCompleteDTO dto) {
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可操作接诊");
        }
        workbenchService.completeConsultation(dto, UserContext.getUserId());
        return Result.success();
    }

    @GetMapping("/schedule-list")
    public Result<List<Schedule>> getScheduleList() {
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可访问");
        }
        return Result.success(workbenchService.getDoctorSchedule(UserContext.getUserId()));
    }

    /**
     * 4. 医生获取历史接诊记录
     */
    @GetMapping("/history-list")
    public Result<List<DoctorOrderVO>> getHistoryList() {
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可访问");
        }
        return Result.success(workbenchService.getHistoryOrderList(UserContext.getUserId()));
    }
}