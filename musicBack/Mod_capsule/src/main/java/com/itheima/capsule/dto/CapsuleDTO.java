package com.itheima.capsule.dto;

import lombok.Data;

/**
 * 创建胶囊请求 DTO
 */
@Data
public class CapsuleDTO {

    /** 接收者 user.id，0 或 null 表示给自己 */
    private Integer receiverId;

    /** 关联 music.music_id */
    private Integer musicId;

    /** 留言内容 */
    private String message;

    /** 解锁时间（ISO 格式 yyyy-MM-ddTHH:mm:ss） */
    private String unlockTime;

    /** 是否公开到广场：false-私密 true-公开 */
    private Boolean isPublic;
}
