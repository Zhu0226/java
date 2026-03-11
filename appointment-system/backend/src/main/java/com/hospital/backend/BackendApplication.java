package com.hospital.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

// 1. 排除 DataSourceAutoConfiguration (拥抱 ShardingSphere)
// 2. 【核心修复】: 排除 SecurityAutoConfiguration，禁用 Spring Security 默认的全局拦截，释放跨域预检！
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.hospital"})
@MapperScan("com.hospital.appointment.mapper")
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}