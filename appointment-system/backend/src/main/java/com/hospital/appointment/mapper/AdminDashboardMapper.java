package com.hospital.appointment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    // 【核心修复2】：移除 sys_doctor 表中不存在的 status = 1 过滤条件
    @Select("SELECT COUNT(1) FROM sys_doctor WHERE is_deleted = 0")
    Integer countActiveDoctors();

    @Select("SELECT IFNULL(SUM(total_num), 0) FROM biz_schedule WHERE work_date = #{today} AND is_deleted = 0")
    Integer sumTodayTotalTickets(@Param("today") LocalDate today);

    @Select("SELECT IFNULL(SUM(total_num - available_num), 0) FROM biz_schedule WHERE work_date = #{today} AND is_deleted = 0")
    Integer sumTodayBookedTickets(@Param("today") LocalDate today);

    @Select("SELECT IFNULL(SUM(amount), 0) FROM biz_order WHERE order_status IN (1, 2) AND is_deleted = 0")
    List<BigDecimal> sumTotalRevenue();
}