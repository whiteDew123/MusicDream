package com.itheima.recommend.vo;

import lombok.Data;

import java.util.List;

/**
 * 歌手详情视图对象
 */
@Data
public class ArtistDetailVO {

    private Integer id;
    private String username;
    private String imageUrl;
    private String about;
    private Integer songCount;
    private Integer totalListen;
    private List<MusicVO> songs;
}
