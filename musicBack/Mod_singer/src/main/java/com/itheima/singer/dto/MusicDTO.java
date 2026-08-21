package com.itheima.singer.dto;

import lombok.Data;

import java.util.List;

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

    /** 标签（逗号分隔，兼容旧接口） */
    private String tags;

    /** 歌词文件URL（旧字段兼容） */
    private String lyric;

    /** 状态: 0-正常, 1-锁定 */
    private Integer activation;

    /** 歌词文件URL（新字段） */
    private String lyricUrl;

    /** 标签列表（新字段，addMusic 使用） */
    private List<String> tagList;
}
