package com.hospital.appointment.model.vo;

import lombok.Data;
import java.time.LocalTime;

/**
 * 医生工作台：患者预约列表视图对象
 */
@Data
public class DoctorOrderVO {
    private String orderNo;

    // 实际企业级开发中，这里会联查 biz_patient 表返回真实姓名。目前我们暂用 ID 替代。
    private Long patientId;

    private Integer ticketNo;
    private LocalTime startTime;
    private LocalTime endTime;

    // 订单状态: 1-已预约待就诊, 2-已就诊
    private Integer orderStatus;
}