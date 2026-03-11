package com.hospital.appointment.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStatsVO {
    private Integer activeDoctors;     // 在职医生数
    private Integer todayTotalTickets; // 今日总放号量
    private Integer todayBookedTickets;// 今日已预约量
    private BigDecimal totalRevenue;   // 历史总营收 (利用 ShardingSphere 跨分片聚合)
}