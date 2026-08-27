package com.itheima.room.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间消息 VO
 */
@Data
public class RoomMessageVO {

    /** 消息ID */
    private Long id;

    /** 发送者用户ID（系统消息为 null） */
    private Long userId;

    /** 发送者昵称 */
    private String username;

    /** 发送者头像 */
    private String imageUrl;

    /** 类型：0-文字 1-Emoji 2-系统 */
    private Integer type;

    /** 内容 */
    private String content;

    /** 房间内消息序列号 */
    private Integer seq;

    /** 发送时间 */
    private LocalDateTime createTime;
}
