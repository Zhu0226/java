package com.hospital.appointment.mapper;

import com.hospital.appointment.model.vo.AdminOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AdminOrderMapper {

    // 【核心修复3】：将 d.doctor_name 改为 d.real_name
    @Select("SELECT o.order_no AS orderNo, o.patient_id AS patientId, o.doctor_id AS doctorId, " +
            "d.real_name AS doctorName, o.amount, o.order_status AS orderStatus, o.create_time AS createTime " +
            "FROM biz_order o " +
            "LEFT JOIN sys_doctor d ON o.doctor_id = d.id " +
            "WHERE o.is_deleted = 0 " +
            "ORDER BY o.create_time DESC LIMIT 100")
    List<AdminOrderVO> selectLatestOrders();
}