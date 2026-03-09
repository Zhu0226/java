package com.hospital.appointment.common;

import lombok.Data;

/**
 * 全局统一返回结果类
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    // 状态码常量定义 (BusinessException 就是在这里找不到 ERROR_CODE)
    public static final Integer SUCCESS_CODE = 200;
    public static final Integer ERROR_CODE = 500;
    // 【新增】专门用于 Token 过期或未登录的状态码
    public static final Integer UNAUTHORIZED_CODE = 401;

    private Result() {}

    // 成功且带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功但不带数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        return result;
    }

    // 【新增】快捷返回 401 的方法
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(UNAUTHORIZED_CODE);
        result.setMessage(message);
        return result;
    }
}