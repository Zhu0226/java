package com.hospital.appointment.interceptor;

import com.hospital.appointment.common.AuthException;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 【核心修复】：放行所有跨域预检请求 (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new AuthException("未携带凭证或格式错误");
        }

        String actualToken = token.substring(7);

        try {
            Claims claims = JwtUtil.parseToken(actualToken);

            // 校验 Token 类型，绝不允许用 Refresh Token 来请求普通业务接口！
            if (!"ACCESS".equals(claims.get("type", String.class))) {
                throw new AuthException("非法的Token类型，请使用 Access Token");
            }

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            UserContext.setUser(userId, role);
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Access Token 已过期, URL: {}", request.getRequestURI());
            throw new AuthException("token_expired");
        } catch (Exception e) {
            log.error("Token解析失败: 被篡改或非法. URL: {}", request.getRequestURI());
            throw new AuthException("凭证已失效，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}