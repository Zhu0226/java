package com.hospital.appointment.mapper;

import com.hospital.appointment.model.vo.ScheduleDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生排班持久层接口 (专注于高频的读操作优化)
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 【企业级高并发读优化】根据科室ID查询可用排班列表
     * 防线1：连表查询消除 N+1 问题。
     * 防线2：严控 status=1 且 is_deleted=0，防止把停诊或已删除的排班放出去。
     *
     * @param deptId 科室ID
     * @param startDate 查询起始日期 (只查今天及以后的)
     * @return 聚合后的视图对象列表
     */
    @Select("SELECT " +
            "  s.id AS scheduleId, " +
            "  d.doctor_name AS doctorName, " +
            "  d.title AS title, " +
            "  d.consultation_fee AS consultationFee, " +
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