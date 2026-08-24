package com.itheima.capsule.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 胶囊视图对象（返回前端）
 */
@Data
public class CapsuleVO {

    private Integer id;
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private Integer musicId;
    private String musicName;
    private String musicUrl;
    private String imageUrl;
    private String singerName;
    private Integer timelength;

    /** 留言内容：封印状态下为 null */
    private String message;

    private LocalDateTime createTime;
    private LocalDateTime unlockTime;

    /** 0-封印 1-已解锁 2-已公开 */
    private Integer status;

    /** 是否公开 */
    private Integer isPublic;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    /** 点赞数 */
    private Integer likeCount;

    /** 距离解锁的倒计时描述 */
    private String countdown;
}
