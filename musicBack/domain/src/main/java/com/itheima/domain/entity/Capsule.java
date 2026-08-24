package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 音乐时空胶囊实体类，对应表 capsule
 */
@Data
@TableName("capsule")
public class Capsule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 发送者 user.id */
    private Integer senderId;

    /** 接收者 user.id，0=给自己 */
    private Integer receiverId;

    /** 关联 music.music_id */
    private Integer musicId;

    /** 留言内容 */
    private String message;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 解锁时间 */
    private LocalDateTime unlockTime;

    /** 0-封印 1-已解锁 2-已公开 */
    private Integer status;

    /** 0-私密 1-公开到广场 */
    private Integer isPublic;
}
