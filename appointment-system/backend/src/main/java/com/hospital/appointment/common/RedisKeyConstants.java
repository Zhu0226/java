package com.hospital.appointment.common;

/**
 * 全局 Redis Key 字典
 * 【大厂规范】：所有的缓存 Key 必须集中管理，加上模块前缀，严禁在业务代码中写死魔法字符串
 */
public class RedisKeyConstants {
    // 科室排班列表缓存前缀 -> hospital:schedule:dept:{deptId}:date:{date}
    public static final String DEPT_SCHEDULE_LIST = "hospital:schedule:dept:";

    // 排班号源池队列 (List结构) -> hospital:ticket:pool:{scheduleId}
    public static final String SCHEDULE_TICKET_POOL = "hospital:ticket:pool:";

    // 分布式锁前缀：防患者重复下单 -> lock:order:submit:{patientId}:{scheduleId}
    public static final String LOCK_ORDER_SUBMIT = "lock:order:submit:";

    // 【新增】双 Token 架构：用户 Refresh Token 存储前缀 -> auth:refresh:token:{userId}
    public static final String AUTH_REFRESH_TOKEN = "auth:refresh:token:";
}