package com.hospital.appointment.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PatientOrderVO {
    private String orderNo;
    private String doctorName;
    private String deptName;
    private BigDecimal amount;
    private Integer orderStatus; // 0-待支付, 1-待就诊, 2-已就诊, 3-已取消
    private LocalDateTime createTime;
}