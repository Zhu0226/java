package com.hospital.appointment.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 线程级别的用户上下文 (基于 ThreadLocal)
 * 作用：在同一个 HTTP 请求的整个生命周期内，安全地共享当前登录用户的 ID 与 角色信息
 */
public class UserContext {

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String role;
    }

    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(Long userId, String role) {
        USER_THREAD_LOCAL.set(new UserInfo(userId, role));
    }

    public static Long getUserId() {
        UserInfo info = USER_THREAD_LOCAL.get();
        return info != null ? info.getUserId() : null;
    }

    public static String getRole() {
        UserInfo info = USER_THREAD_LOCAL.get();
        return info != null ? info.getRole() : null;
    }

    /**
     * 【大厂严苛规范】防止内存泄漏与脏数据！
     * Tomcat 线程池的线程是复用的，请求结束后如果不清理，下一个请求拿到的就是上一个人的数据。
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}