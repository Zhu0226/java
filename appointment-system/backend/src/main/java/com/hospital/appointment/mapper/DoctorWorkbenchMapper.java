package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.Schedule;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DoctorWorkbenchMapper {

    @Select("SELECT o.order_no AS orderNo, o.patient_id AS patientId, " +
            "t.ticket_no AS ticketNo, s.work_date AS workDate, t.start_time AS startTime, t.end_time AS endTime, " +
            "o.order_status AS orderStatus " +
            "FROM biz_order o " +
            "INNER JOIN biz_ticket_pool t ON o.ticket_id = t.id " +
            "INNER JOIN biz_schedule s ON o.schedule_id = s.id " +
            "WHERE o.doctor_id = #{doctorId} AND s.work_date >= #{workDate} " +
            "AND o.order_status IN (1, 2) AND o.is_deleted = 0 " +
            "ORDER BY t.start_time ASC")
    List<DoctorOrderVO> selectDoctorOrdersByDate(@Param("doctorId") Long doctorId, @Param("workDate") LocalDate workDate);

    @Update("UPDATE biz_order SET order_status = 2, version = version + 1, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND doctor_id = #{doctorId} AND order_status = 1 AND version = #{version} AND is_deleted = 0")
    int completeConsultation(@Param("orderNo") String orderNo, @Param("doctorId") Long doctorId, @Param("version") Integer version);

    @Select("SELECT id, doctor_id AS doctorId, dept_id AS deptId, work_date AS workDate, " +
            "shift_type AS shiftType, total_num AS totalNum, available_num AS availableNum, status " +
            "FROM biz_schedule " +
            "WHERE doctor_id = #{doctorId} AND work_date >= #{startDate} " +
            "AND work_date <= #{endDate} AND status = 1 AND is_deleted = 0 " +
            "ORDER BY work_date ASC, shift_type ASC")
    List<Schedule> selectDoctorSchedule(@Param("doctorId") Long doctorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 【新增】：查询该医生所有状态为 2 (已就诊) 的历史订单，按日期倒序排列
    @Select("SELECT o.order_no AS orderNo, o.patient_id AS patientId, " +
            "t.ticket_no AS ticketNo, s.work_date AS workDate, t.start_time AS startTime, t.end_time AS endTime, " +
            "o.order_status AS orderStatus " +
            "FROM biz_order o " +
            "INNER JOIN biz_ticket_pool t ON o.ticket_id = t.id " +
            "INNER JOIN biz_schedule s ON o.schedule_id = s.id " +
            "WHERE o.doctor_id = #{doctorId} AND o.order_status = 2 AND o.is_deleted = 0 " +
            "ORDER BY s.work_date DESC, t.start_time DESC")
    List<DoctorOrderVO> selectHistoryOrders(@Param("doctorId") Long doctorId);
}