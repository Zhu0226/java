package com.seckill.presentation.config;

import com.seckill.presentation.interceptor.AuthInterceptor;
import com.seckill.presentation.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/seckill/preheat/**");

        // 鉴权拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",     // 放行员工登录
                        "/user/login",     // 放行C端买家登录
                        "/user/register",  // 放行C端买家注册
                        "/error",
                        "/seckill/goods/**",
                        "/seckill/status/**"
                );
    }
}