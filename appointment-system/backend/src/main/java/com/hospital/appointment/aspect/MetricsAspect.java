package com.hospital.appointment.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接口请求性能监控切面
 * 作用：利用 Micrometer Timer 精准记录所有 Controller 接口的调用次数、QPS 及响应耗时(P99/P95)。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    /**
     * 拦截 com.hospital.appointment.controller 包下的所有类的所有方法
     */
    @Around("execution(* com.hospital.appointment.controller..*(..))")
    public Object recordMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        boolean isSuccess = true;

        try {
            // 执行目标 Controller 方法
            return joinPoint.proceed();
        } catch (Throwable e) {
            isSuccess = false;
            throw e;
        } finally {
            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 核心：将监控数据注册进 Prometheus 的时序数据库中
            // 指标名称: http_api_request_duration_milliseconds
            // 标签(Tags): class=xxx, method=xxx, status=success/error
            Timer.builder("http.api.request.duration.milliseconds")
                    .description("API Request Duration in Milliseconds")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", isSuccess ? "success" : "error")
                    // 开启百分位统计，这样可以在 Grafana 里画出 P50, P90, P99 的柱状图
                    .publishPercentiles(0.5, 0.9, 0.99)
                    // 如果超过 2 秒就报警
                    .maximumExpectedValue(java.time.Duration.ofSeconds(2))
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            log.debug("📊 [Metrics] API: {}.{} | Status: {} | Duration: {}ms",
                    className, methodName, isSuccess ? "SUCCESS" : "ERROR", duration);
        }
    }
}