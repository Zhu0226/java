package com.hospital.appointment.controller;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.dto.ConsultationCompleteDTO;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import com.hospital.appointment.service.impl.DoctorWorkbenchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * B端接口：医生工作台 API
 * 注意：所有路径前缀使用 /api/doctor 严格与 C端分离
 */
@RestController
@RequestMapping("/api/doctor/workbench")
@RequiredArgsConstructor
public class DoctorWorkbenchController {

    private final DoctorWorkbenchServiceImpl workbenchService;

    /**
     * 1. 医生获取今日待接诊/已接诊列表
     * 【安全修复】：移除 URL 参数 doctorId，防越权。并校验 DOCTOR 角色。
     */
    @GetMapping("/today-list")
    public Result<List<DoctorOrderVO>> getTodayList() {
        // RBAC 鉴权拦截
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可访问工作台");
        }

        // 绝对安全的获取当前登录医生ID
        Long doctorId = UserContext.getUserId();
        if (doctorId == null || doctorId <= 0) {
            return Result.error("医生登录状态异常");
        }
        List<DoctorOrderVO> list = workbenchService.getTodayOrderList(doctorId);
        return Result.success(list);
    }

    /**
     * 2. 医生点击“就诊完成”
     */
    @PostMapping("/complete")
    public Result<Void> completeConsultation(@Validated @RequestBody ConsultationCompleteDTO dto) {
        // RBAC 鉴权拦截
        if (!"DOCTOR".equals(UserContext.getRole())) {
            throw new BusinessException("越权操作：仅医生角色可操作接诊");
        }

        Long currentDoctorId = UserContext.getUserId();
        workbenchService.completeConsultation(dto, currentDoctorId);
        return Result.success();
    }
}