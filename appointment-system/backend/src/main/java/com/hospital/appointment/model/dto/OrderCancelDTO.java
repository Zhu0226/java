package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancelDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    // 【安全修复】：移除前端传入的 patientId，杜绝越权伪造！将通过 UserContext 安全获取
}