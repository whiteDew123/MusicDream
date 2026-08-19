package com.itheima.login.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求 DTO
 * <p>
 * account 支持用户名、邮箱、手机号三种方式登录。
 */
@Data
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录账号（用户名 / 邮箱 / 手机号） */
    private String account;

    /** 密码（明文，由后端 MD5 后比对） */
    private String password;
}
