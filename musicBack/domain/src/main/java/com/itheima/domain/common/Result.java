package com.itheima.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 与网关返回结构保持一致：{"code": 200, "message": "...", "data": ...}
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态码：200 成功，其它为失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 成功响应（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /** 成功响应（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功响应（自定义提示 + 数据） */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 失败响应（自定义状态码 + 提示） */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败响应（默认 500 状态码 + 提示） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败响应（自定义状态码 + 提示），error 为 fail 的别名 */
    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }

    /** 失败响应（默认 500 状态码 + 提示），error 为 fail 的别名 */
    public static <T> Result<T> error(String message) {
        return fail(message);
    }
}