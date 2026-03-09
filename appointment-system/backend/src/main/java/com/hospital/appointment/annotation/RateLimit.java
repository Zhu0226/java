package com.hospital.appointment.annotation;

import java.lang.annotation.*;

/**
 * 接口限流防刷注解
 * 作用：限制某个用户或IP在指定时间内的访问次数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流时间窗口 (默认 60 秒)
     */
    int time() default 60;

    /**
     * 窗口期内允许通过的最大请求数 (默认 3 次)
     */
    int count() default 3;

    /**
     * 触发限流时的提示语
     */
    String message() default "您的操作太频繁，请稍后再试";
}