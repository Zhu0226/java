package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.Schedule;
import com.hospital.appointment.model.entity.TicketPool;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminScheduleMapper {

    @Select("SELECT COUNT(1) FROM biz_schedule WHERE doctor_id = #{doctorId} AND work_date = #{workDate} AND shift_type = #{shiftType} AND is_deleted = 0")
    int checkScheduleExists(@Param("doctorId") Long doctorId, @Param("workDate") LocalDate workDate, @Param("shiftType") Integer shiftType);

    @Insert("INSERT INTO biz_schedule(doctor_id, dept_id, work_date, shift_type, total_num, available_num, status) " +
            "VALUES(#{doctorId}, #{deptId}, #{workDate}, #{shiftType}, #{totalNum}, #{availableNum}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSchedule(Schedule schedule);

    @Insert({
            "<script>",
            "INSERT INTO biz_ticket_pool(schedule_id, ticket_no, start_time, end_time, status, version) VALUES ",
            "<foreach collection='list' item='ticket' separator=','>",
            "(#{ticket.scheduleId}, #{ticket.ticketNo}, #{ticket.startTime}, #{ticket.endTime}, 1, 0)",
            "</foreach>",
            "</script>"
    })
    int batchInsertTicketPool(@Param("list") List<TicketPool> ticketPools);

    // 【核心修复1】：移除 sys_doctor 表中不存在的 status = 1 过滤条件
    @Select("SELECT id as doctorId, dept_id as deptId FROM sys_doctor WHERE is_deleted = 0")
    List<Map<String, Object>> selectAllActiveDoctors();
}