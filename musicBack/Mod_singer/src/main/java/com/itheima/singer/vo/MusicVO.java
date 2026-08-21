package com.itheima.singer.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 歌曲视图对象
 */
@Data
public class MusicVO {

    private Integer musicId;
    private Integer fromSinger;
    private String singerName;
    private String musicName;
    private String musicUrl;
    private Integer activation;
    private Integer auditStatus;
    private String auditRemark;
    private Integer listenNumb;
    private String imageUrl;
    private Integer timelength;
    private LocalDate createTime;
    private String tags;
    private String lyric;
}