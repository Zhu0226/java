package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 挂号订单持久层接口
 * 包含：下单、防重、超时扫描、状态机流转（支付/取消）
 */
@Mapper
public interface OrderMapper {

    // ==========================================
    // 1. 核心创建与查询 (剥离自增ID，拥抱雪花算法分片)
    // ==========================================

    @Insert("INSERT INTO biz_order(id, order_no, patient_id, doctor_id, schedule_id, ticket_id, amount, pay_status, order_status, version, is_deleted, create_time, update_time) " +
            "VALUES(#{id}, #{orderNo}, #{patientId}, #{doctorId}, #{scheduleId}, #{ticketId}, #{amount}, #{payStatus}, #{orderStatus}, 0, 0, NOW(), NOW())")
    int insertOrder(Order order);

    @Select("SELECT COUNT(1) FROM biz_order " +
            "WHERE patient_id = #{patientId} AND schedule_id = #{scheduleId} AND order_status IN (0, 1) AND is_deleted = 0")
    int countValidOrderByPatientAndSchedule(@Param("patientId") Long patientId, @Param("scheduleId") Long scheduleId);

    // ==========================================
    // 2. 超时调度专用 (保留供兜底或后台查询使用)
    // ==========================================
    @Select({
            "<script>",
            "SELECT * FROM biz_order ",
            "WHERE order_status = 0 ",
            // 【核心修复】：将 <= 替换为 &lt;= 解决 XML 解析报错！
            "AND create_time &lt;= #{timeoutThreshold} ",
            "AND is_deleted = 0 ",
            "<if test='failedIds != null and failedIds.size() > 0'>",
            "  AND id NOT IN ",
            "  <foreach collection='failedIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "ORDER BY id ASC ",
            "LIMIT #{limit}",
            "</script>"
    })
    List<Order> selectTimeoutOrders(@Param("timeoutThreshold") LocalDateTime timeoutThreshold,
                                    @Param("limit") int limit,
                                    @Param("failedIds") List<Long> failedIds);

    @Update("UPDATE biz_order " +
            "SET order_status = 4, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND order_status = 0 AND version = #{version} AND is_deleted = 0")
    int cancelTimeoutOrder(@Param("id") Long id, @Param("version") Integer version);

    // ==========================================
    // 3. 支付与主动取消专用 (新增强化防线)
    // ==========================================

    @Select("SELECT * FROM biz_order WHERE order_no = #{orderNo} AND is_deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM biz_order WHERE order_no = #{orderNo} AND patient_id = #{patientId} AND is_deleted = 0")
    Order selectByOrderNoAndPatient(@Param("orderNo") String orderNo, @Param("patientId") Long patientId);

    @Update("UPDATE biz_order " +
            "SET order_status = 1, pay_status = 1, version = version + 1, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND order_status = 0 AND version = #{version} AND is_deleted = 0")
    int updateOrderToPaid(@Param("orderNo") String orderNo, @Param("version") Integer version);

    @Update("UPDATE biz_order " +
            "SET order_status = 3, version = version + 1, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND order_status IN (0, 1) AND version = #{version} AND is_deleted = 0")
    int cancelOrderByPatient(@Param("orderNo") String orderNo, @Param("version") Integer version);
}