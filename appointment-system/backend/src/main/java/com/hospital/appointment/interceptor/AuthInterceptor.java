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
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 注意：这里改抛 AuthException，返回前端 code=401
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
            // 【核心灵魂】：专门捕获过期异常！前端拿到这个信息后，就知道该拿着 Refresh Token 去调 /refresh 接口了
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