package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户实体类
 * <p>
 * 对应数据库 user 表，密码字段已使用 MD5 加密存储。
 * 歌手也是 user，通过 role=1 区分。
 */
@Data
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID，自增 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户名 */
    private String username;

    /** 密码 (MD5 加密) */
    private String password;

    /** 邮箱 (唯一) */
    private String email;

    /** 手机号 (唯一) */
    private String phone;

    /** 角色: 0-管理员, 1-歌手, 2-普通用户 */
    private Integer role;

    /** 激活状态: 0-正常, 1-锁定 */
    private Integer activation;

    /** 创建时间 */
    private LocalDate createTime;

    /** 头像地址 */
    private String imageUrl;

    /** 个人简介 */
    private String about;
}