package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户/歌手实体类，对应表 user
 * 歌手也是 user，通过 role=1 区分。
 */
@Data
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String phone;

    /** 角色: 0-管理员, 1-歌手, 2-普通用户 */
    private Integer role;

    /** 激活状态: 0-正常, 1-锁定 */
    private Integer activation;

    private LocalDate createTime;

    private String imageUrl;

    /** 个人简介 */
    private String about;
}
