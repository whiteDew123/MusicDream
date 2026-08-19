package com.itheima.login.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码（明文，由后端 MD5 加密后入库） */
    private String password;

    /** 邮箱 */
    private String email;

    /** 邮箱验证码 */
    private String emailCode;
}
