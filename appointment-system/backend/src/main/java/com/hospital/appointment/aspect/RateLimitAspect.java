package com.hospital.appointment.aspect;

import com.hospital.appointment.annotation.RateLimit;
import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * 限流防刷切面拦截器
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 1. 获取当前用户 ID（如果未登录则使用 IP 地址）
        Long userId = UserContext.getUserId();
        String identifier = (userId != null) ? userId.toString() : getIpAddress(request);

        // 2. 组装 Redis 限流 Key: rate_limit:类名:方法名:用户标识
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String redisKey = "rate_limit:" + className + ":" + methodName + ":" + identifier;

        // 3. 【企业级高并发核心】执行 Lua 脚本保证判断与自增的原子性
        String luaScript =
                "local current = redis.call('get', KEYS[1]) " +
                        "if current and tonumber(current) >= tonumber(ARGV[1]) then " +
                        "   return tonumber(current) " +
                        "end " +
                        "current = redis.call('incr', KEYS[1]) " +
                        "if tonumber(current) == 1 then " +
                        "   redis.call('expire', KEYS[1], tonumber(ARGV[2])) " +
                        "end " +
                        "return tonumber(current)";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long currentCount = redisTemplate.execute(redisScript, Collections.singletonList(redisKey), rateLimit.count(), rateLimit.time());

        // 4. 判断是否超过限流阈值
        if (currentCount != null && currentCount > rateLimit.count()) {
            log.warn("⚠️ 触发限流防御！IP/User: [{}], 请求接口: [{}]", identifier, request.getRequestURI());
            throw new BusinessException(rateLimit.message());
        }

        // 5. 校验通过，放行执行业务逻辑
        return joinPoint.proceed();
    }

    // 辅助方法：获取真实IP
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}