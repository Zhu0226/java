package com.hospital.appointment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步审计日志持久化服务
 */
@Slf4j
@Service
public class AuditLogServiceImpl {

    /**
     * @Async 指定刚才配置的线程池，实现绝对的异步非阻塞！
     * 不管记录日志多慢，都不会影响前端用户的接口响应时间
     */
    @Async("auditThreadPool")
    public void saveAuditLog(String module, String action, Long userId, String ip,
                             String reqParam, String resData, long timeCost, boolean isSuccess, String errorMsg) {

        // 实际企业开发中，这里会调用 auditLogMapper.insert(...) 保存到 sys_audit_log 表
        // 由于我们目前没有这张表，这里采用格式化的 JSON 日志打印进行代替，以便接入 ELK 系统

        log.info("======== [安全审计留痕入库] ========\n" +
                        "【追踪ID】: {}\n" +
                        "【模  块】: {}\n" +
                        "【动  作】: {}\n" +
                        "【操作人】: {}\n" +
                        "【IP地址】: {}\n" +
                        "【耗  时】: {} ms\n" +
                        "【状  态】: {}\n" +
                        "【入  参】: {}\n" +
                        "【出  参】: {}\n" +
                        "【异  常】: {}\n" +
                        "===================================",
                MDC.get("traceId"), module, action, (userId == null ? "未知用户" : userId),
                ip, timeCost, (isSuccess ? "成功" : "失败"), reqParam, resData, errorMsg);
    }
}