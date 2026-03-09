package com.hospital.appointment.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 作用：防止网络卡顿导致的重复提交、连点问题
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等防重的时间窗口 (默认 5 秒)
     * 在这 5 秒内，参数完全相同的请求将被直接拦截
     */
    int expireTime() default 5;

    /**
     * 触发幂等拦截时的提示语
     */
    String message() default "请求正在处理中，请勿重复点击";
}