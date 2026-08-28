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
 * 好友请求实体
 */
@Data
@TableName("friend_request")
public class FriendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 发送者ID */
    private Integer senderId;

    /** 接收者ID */
    private Integer receiverId;

    /** 验证消息 */
    private String message;

    /** 状态：0-待处理 1-已接受 2-已拒绝 */
    private Integer status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    // ======================== 联表字段（不入库） ========================

    /** 发送者用户名 */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像 */
    @TableField(exist = false)
    private String senderAvatar;

    /** 接收者用户名 */
    @TableField(exist = false)
    private String receiverName;

    /** 接收者头像 */
    @TableField(exist = false)
    private String receiverAvatar;
}