package com.hospital.appointment.mapper;

import com.hospital.appointment.model.vo.ScheduleDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    // 【核心修复2】：将 d.doctor_name 改为 d.real_name，并将缺少的诊查费写死 50.00
    @Select("SELECT " +
            "  s.id AS scheduleId, " +
            "  d.real_name AS doctorName, " +
            "  d.title AS title, " +
            "  50.00 AS consultationFee, " +
            "  s.work_date AS workDate, " +
            "  s.shift_type AS shiftType, " +
            "  s.available_num AS availableNum " +
            "FROM biz_schedule s " +
            "INNER JOIN sys_doctor d ON s.doctor_id = d.id " +
            "WHERE s.dept_id = #{deptId} " +
            "  AND s.work_date >= #{startDate} " +
            "  AND s.status = 1 " +
            "  AND s.is_deleted = 0 " +
            "ORDER BY s.work_date ASC, s.shift_type ASC")
    List<ScheduleDetailVO> selectAvailableSchedulesByDept(@Param("deptId") Long deptId, @Param("startDate") LocalDate startDate);
}