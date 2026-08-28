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
 * 好友会话实体
 * <p>
 * 复用 friend 关系，缓存「最后消息 + 未读数」避免每次扫消息表聚合。
 * 双向会话设计：A↔B 好友生成 2 条记录（user_id=A,friend_id=B 和 user_id=B,friend_id=A），
 * 各管自己的未读数。
 */
@Data
@TableName("friend_conversation")
public class FriendConversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话所属用户ID（查看方） */
    private Integer userId;

    /** 对方用户ID */
    private Integer friendId;

    /** 最后一条消息预览 */
    private String lastMessage;

    /** 最后消息时间 */
    private Date lastMsgTime;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    // ======================== 联表字段（不入库） ========================

    /** 好友用户名 */
    @TableField(exist = false)
    private String friendName;

    /** 好友头像 */
    @TableField(exist = false)
    private String friendAvatar;

    /** 好友在线状态: 1=在线 0=离线（由 Redis 实时填充） */
    @TableField(exist = false)
    private Integer friendOnline;
}
