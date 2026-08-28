package com.itheima.musicbox.vo;

import lombok.Data;

@Data
public class MusicBoxSongVO {
    private Integer songId;
    private String songName;
    private String singerName;
    private String coverUrl;
    private Integer sortOrder;
}