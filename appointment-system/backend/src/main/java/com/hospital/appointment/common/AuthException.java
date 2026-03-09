package com.hospital.appointment.common;

/**
 * 专属认证异常类
 */
public class AuthException extends RuntimeException {
    private final Integer code;

    public AuthException(String message) {
        super(message);
        this.code = Result.UNAUTHORIZED_CODE; // 固定为 401
    }

    public Integer getCode() {
        return code;
    }
}