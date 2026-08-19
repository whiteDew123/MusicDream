package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * <p>
 * 将业务层抛出的 IllegalArgumentException / IllegalStateException 转换为友好的 Result 响应，
 * 与网关返回结构 {"code", "message", "data"} 保持一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务参数 / 状态错误 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Result<Void> handleBusinessException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /** 其他未捕获异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误，请稍后重试");
    }
}
