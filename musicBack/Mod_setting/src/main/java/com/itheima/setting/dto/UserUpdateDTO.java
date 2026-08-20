package com.itheima.setting.dto;

import lombok.Data;

/**
 * 用户信息设置请求参数
 */
@Data
public class UserUpdateDTO {

    /** 用户名 */
    private String username;

    /** 邮箱（唯一） */
    private String email;

    /** 手机号（唯一） */
    private String phone;

    /** 个人简介 */
    private String about;
}
