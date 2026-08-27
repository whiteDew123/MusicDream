package com.itheima.room.dto;

import lombok.Data;

/**
 * 添加歌曲到房间歌单请求体
 */
@Data
public class PlaylistAddDTO {

    /** 歌曲ID */
    private Long musicId;
}
