package com.hospital.appointment.controller;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.mapper.OrderMapper;
import com.hospital.appointment.mapper.PatientMapper;
import com.hospital.appointment.model.entity.SysPatient;
import com.hospital.appointment.model.vo.PatientProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;

@RestController
@RequestMapping("/api/patient/profile")
@RequiredArgsConstructor
public class PatientController {

    private final PatientMapper patientMapper;
    private final OrderMapper orderMapper;

    @GetMapping("/info")
    public Result<PatientProfileVO> getProfileInfo() {
        // 获取当前登录患者的业务ID (在AuthServiceImpl签发Token时存入的)
        Long patientId = UserContext.getUserId();
        if (patientId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        SysPatient patient = patientMapper.findById(patientId);
        if (patient == null) {
            throw new BusinessException("未找到患者信息");
        }

        PatientProfileVO vo = new PatientProfileVO();
        vo.setRealName(patient.getRealName());
        vo.setGender(patient.getGender());

        // 1. 动态计算年龄
        if (patient.getBirthDate() != null) {
            vo.setAge(Period.between(patient.getBirthDate(), LocalDate.now()).getYears());
        } else {
            vo.setAge(0);
        }

        // 2. 手机号与身份证号脱敏打码处理
        if (patient.getPhone() != null && patient.getPhone().length() == 11) {
            // 将 13812345678 转换成 138****5678
            vo.setPhone(patient.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        if (patient.getIdCard() != null && patient.getIdCard().length() == 18) {
            // 将身份证中间部分全部用星号替换，如 110105**********12
            vo.setIdCard(patient.getIdCard().replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1**********$2"));
        }

        // 3. 统计各状态的订单数
        vo.setPendingPayCount(orderMapper.countOrdersByStatus(patientId, 0));   // 0-待支付
        vo.setPendingVisitCount(orderMapper.countOrdersByStatus(patientId, 1)); // 1-待就诊
        vo.setCompletedCount(orderMapper.countOrdersByStatus(patientId, 2));    // 2-已完成

        return Result.success(vo);
    }
}