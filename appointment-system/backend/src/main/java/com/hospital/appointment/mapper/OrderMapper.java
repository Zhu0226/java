package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.Order;
import com.hospital.appointment.model.vo.PatientOrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT COUNT(1) FROM biz_order WHERE patient_id = #{patientId} AND schedule_id = #{scheduleId} AND order_status IN (0, 1) AND is_deleted = 0")
    int countValidOrderByPatientAndSchedule(@Param("patientId") Long patientId, @Param("scheduleId") Long scheduleId);

    @Insert("INSERT INTO biz_order(id, order_no, patient_id, doctor_id, schedule_id, ticket_id, amount, pay_status, order_status, version, is_deleted, create_time, update_time) " +
            "VALUES(#{id}, #{orderNo}, #{patientId}, #{doctorId}, #{scheduleId}, #{ticketId}, #{amount}, #{payStatus}, #{orderStatus}, 0, 0, NOW(), NOW())")
    int insertOrder(Order order);

    @Select("SELECT * FROM biz_order WHERE order_no = #{orderNo} AND is_deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE biz_order SET order_status = 3, version = version + 1, update_time = NOW() WHERE id = #{id} AND version = #{version} AND order_status = 0 AND is_deleted = 0")
    int cancelTimeoutOrder(@Param("id") Long id, @Param("version") Integer version);

    @Update("UPDATE biz_order SET pay_status = 1, order_status = 1, version = version + 1, update_time = NOW() WHERE order_no = #{orderNo} AND version = #{version} AND is_deleted = 0")
    int updateOrderToPaid(@Param("orderNo") String orderNo, @Param("version") Integer version);

    @Select("SELECT * FROM biz_order WHERE order_no = #{orderNo} AND patient_id = #{patientId} AND is_deleted = 0")
    Order selectByOrderNoAndPatient(@Param("orderNo") String orderNo, @Param("patientId") Long patientId);

    @Update("UPDATE biz_order SET order_status = 3, version = version + 1, update_time = NOW() WHERE order_no = #{orderNo} AND version = #{version} AND order_status IN (0, 1) AND is_deleted = 0")
    int cancelOrderByPatient(@Param("orderNo") String orderNo, @Param("version") Integer version);

    // 【核心修复1】：改为 d.real_name，并恢复之前被我们剔除的 sys_dept 科室连表查询！
    @Select("SELECT o.order_no AS orderNo, d.real_name AS doctorName, dep.dept_name AS deptName, " +
            "o.amount, o.order_status AS orderStatus, o.create_time AS createTime " +
            "FROM biz_order o " +
            "JOIN sys_doctor d ON o.doctor_id = d.id " +
            "JOIN sys_dept dep ON d.dept_id = dep.id " +
            "WHERE o.patient_id = #{patientId} AND o.is_deleted = 0 " +
            "ORDER BY o.create_time DESC")
    List<PatientOrderVO> selectPatientOrders(@Param("patientId") Long patientId);

    // 【新增】：根据排班ID查询真正的医生ID，用来干掉写死的 888L
    @Select("SELECT doctor_id FROM biz_schedule WHERE id = #{scheduleId}")
    Long selectDoctorIdByScheduleId(@Param("scheduleId") Long scheduleId);

    @Select("SELECT COUNT(1) FROM biz_order WHERE patient_id = #{patientId} AND order_status = #{status} AND is_deleted = 0")
    int countOrdersByStatus(@Param("patientId") Long patientId, @Param("status") Integer status);
}