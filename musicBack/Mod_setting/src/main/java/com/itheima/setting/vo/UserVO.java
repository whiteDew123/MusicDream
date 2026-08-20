package com.itheima.setting.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户信息视图对象（不返回密码）
 */
@Data
public class UserVO {

    private Integer id;
    private String username;
    private String email;
    private String phone;
    private Integer role;
    private Integer activation;
    private LocalDate createTime;
    private String imageUrl;
    private String about;
}
