package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.TicketPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 号源池持久层接口
 */
@Mapper
public interface TicketPoolMapper {

    /**
     * 查询某排班下所有可用的号源 (旧版纯 DB 防超卖使用，保留用于兜底或后台对账)
     */
    @Select("SELECT * FROM biz_ticket_pool WHERE schedule_id = #{scheduleId} AND status = 1 AND is_deleted = 0 ORDER BY ticket_no ASC")
    List<TicketPool> selectAvailableTickets(@Param("scheduleId") Long scheduleId);

    /**
     * 核心攻坚：使用乐观锁锁定号源 (防并发超卖 - 旧版使用)
     * 逻辑：只有当传入的 version 与数据库当前的 version 一致时，才允许更新状态为“已锁定(2)”
     * @param id 号源明细ID
     * @param version 期望的版本号(从数据库刚查出来的那个)
     * @return 影响的行数。如果返回 0，说明被别人抢先修改了，发生并发冲突。
     */
    @Update("UPDATE biz_ticket_pool " +
            "SET status = 2, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND status = 1 AND is_deleted = 0")
    int lockTicketWithVersion(@Param("id") Long id, @Param("version") Integer version);

    /**
     * 【高并发架构升级版】：根据明确的号源 ID 直接锁定状态
     * 业务背景：在秒杀/抢号链路中，当前号源 ID 是从 Redis List 中通过原子操作 leftPop 弹出来的。
     * 因为 Redis 是单线程的，弹出动作天然具备排他性（同一个 ID 绝不会被两个人同时弹出来）。
     * 所以一旦拿到 ID，在数据库层面就不需要再用版本号死磕尝试了，直接根据 ID 锁定即可，大大降低 DB 负担！
     * * @param id 从 Redis 中拿到的可用号源明细ID
     * @return 影响的行数。如果为 0，说明出现了极端的数据不一致（Redis有数据但DB没数据/状态不对），触发补偿回滚。
     */
    @Update("UPDATE biz_ticket_pool " +
            "SET status = 2, version = version + 1 " +
            "WHERE id = #{id} AND status = 1 AND is_deleted = 0")
    int lockTicketById(@Param("id") Long id);

    /**
     * 释放超时未支付的号源 / 释放患者主动取消的号源
     * 注意：不仅要在 DB 释放，在 OrderServiceImpl 中还要配合将该号源 ID 重新 rightPush 回 Redis 队列中！
     *
     * @param id 号源明细ID
     * @return 影响的行数
     */
    @Update("UPDATE biz_ticket_pool " +
            "SET status = 1 " +
            "WHERE id = #{id} AND status = 2 AND is_deleted = 0")
    int releaseTicket(@Param("id") Long id);
}