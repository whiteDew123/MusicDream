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
 * 好友消息实体
 */
@Data
@TableName("friend_message")
public class FriendMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送者ID */
    private Integer senderId;

    /** 接收者ID */
    private Integer receiverId;

    /** 消息内容 */
    private String content;

    /** 消息类型: 0=文本 1=图片 2=音频 3=歌曲分享 */
    private Integer msgType;

    /** 是否已读: 0=未读 1=已读 */
    private Integer isRead;

    /** 发送时间 */
    private Date createTime;

    // ======================== 联表字段（不入库） ========================

    /** 发送者用户名 */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像 */
    @TableField(exist = false)
    private String senderAvatar;
}
