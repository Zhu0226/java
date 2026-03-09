package com.hospital.appointment.model.entity;

import lombok.Data;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 号源池明细实体类 (对应表: biz_ticket_pool)
 */
@Data
public class TicketPool {
    private Long id;
    private Long scheduleId;
    private Integer ticketNo;
    private LocalTime startTime;
    private LocalTime endTime;

    /**
     * 状态: 0-未放号, 1-可预约, 2-已锁定(防超卖中间态), 3-已售出
     */
    private Integer status;

    /**
     * 乐观锁版本号，拦截并发冲突的核心字段
     */
    private Integer version;

    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}