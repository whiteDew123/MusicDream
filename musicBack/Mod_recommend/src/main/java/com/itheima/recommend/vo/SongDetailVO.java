package com.itheima.recommend.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 歌曲详情视图对象
 */
@Data
public class SongDetailVO {

    private Integer musicId;
    private Integer fromSinger;
    private String singerName;
    private String singerImageUrl;
    private String singerAbout;
    private String musicName;
    private String musicUrl;
    private Integer activation;
    private Integer listenNumb;
    private String imageUrl;
    private Integer timelength;
    private LocalDate createTime;
    private String tags;
    private String lyric;
}
