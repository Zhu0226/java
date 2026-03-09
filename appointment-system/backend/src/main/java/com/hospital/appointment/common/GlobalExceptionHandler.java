package com.hospital.appointment.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器：拦截 Controller 层抛出的所有异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【新增】拦截认证异常 (返回 401 状态，触发前端无感刷新机制)
     */
    @ExceptionHandler(AuthException.class)
    public Result<Void> handleAuthException(AuthException e) {
        log.warn("鉴权拦截: {}", e.getMessage());
        return Result.unauthorized(e.getMessage());
    }

    /**
     * 拦截业务异常 (比如抢号失败、重复挂号)
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常拦截: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 拦截参数校验异常 (比如 DTO 里的 @NotNull 没通过)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验不通过: {}", errorMsg);
        return Result.error("参数错误: " + errorMsg);
    }

    /**
     * 兜底拦截：处理所有未知的系统异常 (防止代码 Bug 暴露给前端)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleSystemException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 拦截 404 资源未找到异常 (包括浏览器偷偷请求的 favicon.ico)
     * 企业级优化：降级为 Warn 级别日志，且不打印堆栈，防止日志刷屏
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.warn("请求的接口或静态资源不存在: {}", e.getResourcePath());
        return Result.error("路径不存在: /" + e.getResourcePath());
    }
}