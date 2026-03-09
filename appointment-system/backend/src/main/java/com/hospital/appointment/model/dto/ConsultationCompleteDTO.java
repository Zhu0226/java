package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 医生完成接诊请求参数
 */
@Data
public class ConsultationCompleteDTO {
    @NotBlank(message = "业务订单号不能为空")
    private String orderNo;

    // 【安全修复】：移除前端传入的 doctorId，杜绝越权伪造！将通过 UserContext 安全获取
}