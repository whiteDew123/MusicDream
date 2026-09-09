package com.itheima.friend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系实体
 */
@Data
@TableName("friend")
public class Friend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID */
    private Integer userId;

    /** 好友ID */
    private Integer friendId;

    /** 成为好友时间 */
    private Date createTime;

    // ======================== 联表字段（不入库） ========================

    /** 好友用户名 */
    @TableField(exist = false)
    private String friendName;

    /** 好友头像 */
    @TableField(exist = false)
    private String friendAvatar;
}