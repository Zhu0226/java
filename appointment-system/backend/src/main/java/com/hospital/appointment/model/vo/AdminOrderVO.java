package com.hospital.appointment.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderVO {
    private String orderNo;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private BigDecimal amount;
    // 0-待支付, 1-待就诊, 2-已就诊, 3-已取消
    private Integer orderStatus;
    private LocalDateTime createTime;
}