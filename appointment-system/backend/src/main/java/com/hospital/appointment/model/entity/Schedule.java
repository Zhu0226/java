package com.hospital.appointment.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生排班实体类 (对应表: biz_schedule)
 */
@Data
public class Schedule {
    private Long id;
    private Long doctorId;
    private Long deptId;
    private LocalDate workDate;

    /** 班次: 1-上午, 2-下午, 3-夜诊 */
    private Integer shiftType;

    private Integer totalNum;
    private Integer availableNum;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}