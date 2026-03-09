package com.hospital.appointment.config;

import com.hospital.appointment.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有 /api 下的请求
                .addPathPatterns("/api/**")
                // 白名单：登录、刷新Token、患者查排班、系统监控接口不需要拦截
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/schedule/list",
                        "/actuator/**"   // 【核心修改】：放行监控大屏采集路由！
                );
    }
}