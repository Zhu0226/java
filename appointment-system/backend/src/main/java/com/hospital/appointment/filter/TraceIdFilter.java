package com.hospital.appointment.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 企业级全链路日志追踪过滤器
 * 作用：为每一个 HTTP 请求生成/提取全局唯一的 TraceId，并注入到日志上下文中
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 尝试从请求头获取 TraceId (微服务场景下通常由网关传入)
            String traceId = request.getHeader("X-Trace-Id");
            if (!StringUtils.hasText(traceId)) {
                // 2. 如果没有，则自动生成一个简短的 UUID
                traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }

            // 3. 将 TraceId 放入 SLF4J 的 MDC (Mapped Diagnostic Context) 上下文中
            MDC.put(TRACE_ID, traceId);

            // 4. 将 TraceId 放入响应头，方便前端排查报错时提供给后端
            response.setHeader("X-Trace-Id", traceId);

            // 5. 放行请求
            filterChain.doFilter(request, response);
        } finally {
            // 6. 【大厂规范】请求结束必须清理 MDC，防止 Tomcat 线程池复用导致 TraceId 串位！
            MDC.clear();
        }
    }
}