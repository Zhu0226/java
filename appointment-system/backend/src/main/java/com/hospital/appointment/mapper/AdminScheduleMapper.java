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

/**
 * 管理后台专属：排班与号源自动化持久层
 */
@Mapper
public interface AdminScheduleMapper {

    /**
     * 防重校验：检查某医生在某天某班次是否已经生成过排班
     */
    @Select("SELECT COUNT(1) FROM biz_schedule WHERE doctor_id = #{doctorId} AND work_date = #{workDate} AND shift_type = #{shiftType} AND is_deleted = 0")
    int checkScheduleExists(@Param("doctorId") Long doctorId, @Param("workDate") LocalDate workDate, @Param("shiftType") Integer shiftType);

    /**
     * 插入主排班表，并回写自增主键 ID (用于后续关联号源池)
     */
    @Insert("INSERT INTO biz_schedule(doctor_id, dept_id, work_date, shift_type, total_num, available_num, status) " +
            "VALUES(#{doctorId}, #{deptId}, #{workDate}, #{shiftType}, #{totalNum}, #{availableNum}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSchedule(Schedule schedule);

    /**
     * 【企业级高阶技巧】MyBatis 批量插入号源池 (Bulk Insert)
     * 利用 <script> 和 <foreach> 标签，将多条数据合并为一条庞大的 INSERT SQL 执行，性能提升百倍。
     */
    @Insert({
            "<script>",
            "INSERT INTO biz_ticket_pool(schedule_id, ticket_no, start_time, end_time, status, version) VALUES ",
            "<foreach collection='list' item='ticket' separator=','>",
            "(#{ticket.scheduleId}, #{ticket.ticketNo}, #{ticket.startTime}, #{ticket.endTime}, 1, 0)",
            "</foreach>",
            "</script>"
    })
    int batchInsertTicketPool(@Param("list") List<TicketPool> ticketPools);

    /**
     * 查询所有在职医生的精简信息 (用于自动化跑批任务)
     * 返回 Map，包含 id 和 dept_id
     */
    @Select("SELECT id as doctorId, dept_id as deptId FROM sys_doctor WHERE status = 1 AND is_deleted = 0")
    List<Map<String, Object>> selectAllActiveDoctors();
}