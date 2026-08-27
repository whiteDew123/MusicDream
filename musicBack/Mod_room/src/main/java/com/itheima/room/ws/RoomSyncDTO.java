package com.itheima.room.ws;

import lombok.Data;

/**
 * 播放同步上报（房主 → 服务端 → 广播成员）
 */
@Data
public class RoomSyncDTO {

    /** 当前歌曲ID */
    private Long musicId;

    /** 当前进度（秒） */
    private Double progress;

    /** 是否播放中：0-暂停 1-播放 */
    private Integer isPlaying;
}
