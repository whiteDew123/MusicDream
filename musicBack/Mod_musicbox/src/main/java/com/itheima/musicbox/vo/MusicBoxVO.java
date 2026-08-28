package com.itheima.musicbox.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MusicBoxVO {
    private Integer id;
    private Integer userId;
    private String title;
    private String moodTag;
    private String message;
    private String coverUrl;
    private Integer openCount;
    private Integer likeCount;
    private LocalDateTime createTime;
    private List<MusicBoxSongVO> songs;
    private Boolean isLiked;
    private Boolean isOpened;
}