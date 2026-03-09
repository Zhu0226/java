package com.hospital.appointment.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态机枚举 (严格控制状态流转)
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    UNPAID(0, "待支付"),
    APPOINTED(1, "已预约(待就诊)"),
    FINISHED(2, "已就诊"),
    CANCELLED_BY_PATIENT(3, "患者主动取消"),
    CANCELLED_BY_TIMEOUT(4, "超时自动取消");

    private final Integer code;
    private final String desc;
}