package com.itheima.musicbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_box")
public class MusicBox {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String moodTag;
    private String message;
    private String coverUrl;
    private Integer openCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
}