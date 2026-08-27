package com.itheima.room.ws;

import lombok.Data;

/**
 * 聊天消息（成员 → 服务端）
 */
@Data
public class RoomChatDTO {

    /** 类型：0-文字 1-Emoji 2-系统 */
    private Integer type;

    /** 内容 */
    private String content;
}
