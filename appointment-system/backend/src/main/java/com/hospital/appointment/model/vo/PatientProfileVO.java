package com.hospital.appointment.model.vo;

import lombok.Data;

@Data
public class PatientProfileVO {
    private String realName;
    private String phone;
    private String idCard;
    private Integer gender; // 1-男, 2-女
    private Integer age;    // 动态计算出的年龄

    // 订单统计角标
    private Integer pendingPayCount;
    private Integer pendingVisitCount;
    private Integer completedCount;
}