package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.Order;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生工作台专属持久层
 */
@Mapper
public interface DoctorWorkbenchMapper {

    /**
     * 【复杂多表联查】获取医生某一天的接诊列表
     * 联查了 订单表、号源表、排班表，一次性聚合前端需要的所有展示数据。
     */
    @Select("SELECT " +
            "  o.order_no AS orderNo, " +
            "  o.patient_id AS patientId, " +
            "  t.ticket_no AS ticketNo, " +
            "  t.start_time AS startTime, " +
            "  t.end_time AS endTime, " +
            "  o.order_status AS orderStatus " +
            "FROM biz_order o " +
            "INNER JOIN biz_ticket_pool t ON o.ticket_id = t.id " +
            "INNER JOIN biz_schedule s ON o.schedule_id = s.id " +
            "WHERE o.doctor_id = #{doctorId} " +
            "  AND s.work_date = #{workDate} " +
            "  AND o.order_status IN (1, 2) " + // 只看已付款待就诊(1)和已就诊(2)的，不看退号的
            "  AND o.is_deleted = 0 " +
            "ORDER BY t.start_time ASC")
    List<DoctorOrderVO> selectDoctorOrdersByDate(@Param("doctorId") Long doctorId, @Param("workDate") LocalDate workDate);

    /**
     * 【状态机防线】医生完成接诊，更新订单状态
     * 必须卡死两个条件：必须是当前医生的订单(防越权)，且当前状态必须是待就诊(1)。
     */
    @Update("UPDATE biz_order " +
            "SET order_status = 2, version = version + 1, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND doctor_id = #{doctorId} AND order_status = 1 AND version = #{version} AND is_deleted = 0")
    int completeConsultation(@Param("orderNo") String orderNo, @Param("doctorId") Long doctorId, @Param("version") Integer version);
}