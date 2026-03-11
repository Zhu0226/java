package com.hospital.appointment.config;

import com.hospital.appointment.mapper.UserMapper;
import com.hospital.appointment.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("⏳ 正在检查系统基础数据...");

        // 让 Java 程序亲自去查一遍 admin 在不在
        SysUser admin = userMapper.findByUsername("admin");

        if (admin == null) {
            log.info("🚀 未检测到 admin 账号，系统正在自动初始化超级管理员...");

            SysUser user = new SysUser();
            user.setId(1L);
            user.setUsername("admin");
            // 【最关键的一步】：调用系统自身的 PasswordEncoder 去加密 123456，保证解密时绝对匹配！
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole("ADMIN");
            user.setStatus(1);

            userMapper.insert(user);
            log.info("✅ 超级管理员 admin 初始化完成！请在前端使用账号: admin, 密码: 123456 登录。");
        } else {
            log.info("✅ 数据库中已存在 admin 账号，无需重复初始化。");
        }
    }
}