package com.hospital.appointment.service.impl;

import com.hospital.appointment.mapper.AdminDashboardMapper;
import com.hospital.appointment.model.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardMapper dashboardMapper;

    public DashboardStatsVO getGlobalStats() {
        LocalDate today = LocalDate.now();
        DashboardStatsVO stats = new DashboardStatsVO();

        stats.setActiveDoctors(dashboardMapper.countActiveDoctors());
        stats.setTodayTotalTickets(dashboardMapper.sumTodayTotalTickets(today));
        stats.setTodayBookedTickets(dashboardMapper.sumTodayBookedTickets(today));

        // 【修改这里】：手动累加各个分片返回的营收结果
        List<BigDecimal> revenueList = dashboardMapper.sumTotalRevenue();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (revenueList != null && !revenueList.isEmpty()) {
            for (BigDecimal revenue : revenueList) {
                if (revenue != null) {
                    totalRevenue = totalRevenue.add(revenue);
                }
            }
        }
        stats.setTotalRevenue(totalRevenue);

        log.info("📈 全局数据看板加载完毕: {}", stats);
        return stats;
    }
}