package com.hospital.appointment.common;

/**
 * 全局业务异常类：当抢号失败或校验不通过时抛出，由全局异常处理器拦截
 */
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        // 这里需要调用同包下 Result 类的 ERROR_CODE 常量
        this.code = Result.ERROR_CODE;
    }

    public Integer getCode() {
        return code;
    }
}