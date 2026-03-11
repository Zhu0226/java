package com.hospital.appointment.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 医生工作台：患者预约列表视图对象
 */
@Data
public class DoctorOrderVO {
    private String orderNo;
    private Long patientId;
    private Integer ticketNo;
    // 【新增】：历史记录需要的就诊日期
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    // 订单状态: 1-已预约待就诊, 2-已就诊
    private Integer orderStatus;
}