package com.hospital.appointment.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.annotation.Idempotent;
import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 接口幂等性切面拦截器
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = UserContext.getUserId();
        if (userId == null) {
            return joinPoint.proceed();
        }

        String uri = request.getRequestURI();

        // 【安全修复】：过滤掉不能被序列化的对象，防止序列化炸弹
        List<Object> safeArgs = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof HttpServletRequest)
                        && !(arg instanceof HttpServletResponse)
                        && !(arg instanceof MultipartFile))
                .collect(Collectors.toList());

        String argsJson = objectMapper.writeValueAsString(safeArgs);
        String md5Fingerprint = DigestUtils.md5DigestAsHex(argsJson.getBytes());

        String redisKey = "idempotent:" + userId + ":" + uri + ":" + md5Fingerprint;

        Boolean isFirstRequest = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", idempotent.expireTime(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            log.warn("🛡️ 触发幂等防御！拦截用户[{}]的重复请求: [{}]", userId, uri);
            throw new BusinessException(idempotent.message());
        }

        return joinPoint.proceed();
    }
}