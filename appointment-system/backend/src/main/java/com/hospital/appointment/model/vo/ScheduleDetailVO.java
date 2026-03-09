package com.hospital.appointment.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班详情视图对象 (View Object)
 * 作用：严格隔离数据库底层实体，聚合前端展示所需的跨表字段
 */
@Data
public class ScheduleDetailVO {

    /** 排班业务主键 (供前端下单时传给后端) */
    private Long scheduleId;

    /** 医生姓名 (来自 sys_doctor 表) */
    private String doctorName;

    /** 医生职称 */
    private String title;

    /** 诊查费 (企业级金额必须用 BigDecimal) */
    private BigDecimal consultationFee;

    /** 出诊日期 */
    private LocalDate workDate;

    /** 班次: 1-上午, 2-下午, 3-夜诊 */
    private Integer shiftType;

    /** 剩余可用号源数 (前端根据此字段判断是否显示"已满") */
    private Integer availableNum;
}