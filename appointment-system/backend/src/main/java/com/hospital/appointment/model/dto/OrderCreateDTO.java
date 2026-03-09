package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建挂号订单入参 DTO
 * 【企业级安全升级】：绝对不暴露 patientId 字段给前端，防范 BOLA (越权) 攻击
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "医生排班不能为空")
    private Long scheduleId;

    // 注意：患者ID (patientId) 将在 Service 层直接从 JWT 解析后的 UserContext 中安全获取
}