package com.hospital.appointment.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 挂号订单实体类 (对应表: biz_order)
 */
@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long patientId;
    private Long doctorId;
    private Long scheduleId;
    private Long ticketId;

    /**
     * 订单金额 (必须使用 BigDecimal 防精度丢失)
     */
    private BigDecimal amount;

    private Integer payStatus;
    private Integer orderStatus;
    private Integer version;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}