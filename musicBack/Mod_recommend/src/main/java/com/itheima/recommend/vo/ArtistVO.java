package com.itheima.recommend.vo;

import lombok.Data;

/**
 * 歌手推荐视图对象
 */
@Data
public class ArtistVO {

    private Integer id;
    private String username;
    private String imageUrl;
    private String about;
    private Integer songCount;
    private Integer totalListen;
}
