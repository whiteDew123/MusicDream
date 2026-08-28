package com.itheima.musicbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_box_like")
public class MusicBoxLike {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer boxId;
    private Integer userId;
    private LocalDateTime createTime;
}