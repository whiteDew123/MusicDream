package com.itheima.singer.dto;

import lombok.Data;

/**
 * 歌曲新增/修改请求参数
 */
@Data
public class MusicDTO {

    /** 歌手ID，对应 user.id */
    private Integer fromSinger;

    /** 歌曲名 */
    private String musicName;

    /** 音频文件URL */
    private String musicUrl;

    /** 封面图片URL */
    private String imageUrl;

    /** 时长（秒） */
    private Integer timelength;

    /** 标签（逗号分隔） */
    private String tags;

    /** 歌词文件URL */
    private String lyric;
}
