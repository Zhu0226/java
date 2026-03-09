package com.hospital.appointment.annotation;

import java.lang.annotation.*;

/**
 * 核心操作审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    /** 模块名称，如 "排班管理" */
    String module() default "";

    /** 具体操作，如 "手动触发全院跑批" */
    String action() default "";
}