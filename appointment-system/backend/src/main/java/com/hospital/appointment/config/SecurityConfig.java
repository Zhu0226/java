package com.hospital.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码安全配置
 */
@Configuration
public class SecurityConfig {

    /**
     * 注入大厂标配的 BCrypt 密码加密器
     * 特点：即使两个人的明文密码都是 "123456"，因为内部自带随机盐(Salt)，
     * 生成的密文也完全不一样，彻底杜绝彩虹表破解！
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}