package com.itheima.room.ws;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 播放状态广播消息
 */
@Data
public class RoomStateMessage {

    /** 当前歌曲ID */
    private Long musicId;

    /** 当前进度（秒） */
    private Double progress;

    /** 是否播放中：0-暂停 1-播放 */
    private Integer isPlaying;

    /** 房间状态：0-空闲 1-播放中 2-已结束 */
    private Integer status;

    /** 广播时间（用于客户端判断延迟） */
    private LocalDateTime at;
}
