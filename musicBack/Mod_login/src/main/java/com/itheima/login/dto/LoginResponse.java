package com.itheima.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** JWT token */
    private String token;

    /** 用户ID */
    private Integer userId;

    /** 用户名 */
    private String username;

    /** 角色: 0-管理员, 1-歌手, 2-普通用户 */
    private Integer role;

    /** 头像地址 */
    private String imageUrl;
}
