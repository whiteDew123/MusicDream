package com.itheima.musicbox.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MusicBoxPlazaVO {
    private Integer id;
    private String title;
    private String moodTag;
    private String coverUrl;
    private Integer openCount;
    private Integer likeCount;
    private Integer songCount;
    private LocalDateTime createTime;
    private Boolean isLiked;
}