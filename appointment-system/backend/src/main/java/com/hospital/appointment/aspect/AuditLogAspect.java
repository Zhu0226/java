package com.hospital.appointment.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.annotation.AuditLog;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.service.impl.AuditLogServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务审计留痕切面拦截器
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogServiceImpl auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        boolean isSuccess = true;
        String errorMsg = "";
        Object result = null;

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Long userId = UserContext.getUserId();
        String ip = getIpAddress(request);
        String reqParam = "";
        String resData = "";

        // 【安全修复】：过滤不能被序列化的底层流对象
        List<Object> safeArgs = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof HttpServletRequest)
                        && !(arg instanceof HttpServletResponse)
                        && !(arg instanceof MultipartFile))
                .collect(Collectors.toList());

        try {
            reqParam = objectMapper.writeValueAsString(safeArgs);
        } catch (Exception e) {
            reqParam = "参数序列化失败";
        }

        try {
            result = joinPoint.proceed();
            try {
                resData = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                resData = "结果序列化失败";
            }
            return result;
        } catch (Throwable throwable) {
            isSuccess = false;
            errorMsg = throwable.getMessage();
            throw throwable;
        } finally {
            long timeCost = System.currentTimeMillis() - startTime;
            auditLogService.saveAuditLog(auditLog.module(), auditLog.action(), userId, ip,
                    reqParam, resData, timeCost, isSuccess, errorMsg);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}