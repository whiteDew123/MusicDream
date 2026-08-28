package com.itheima.musicbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("music_box_song")
public class MusicBoxSong {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer boxId;
    private Integer songId;
    private Integer sortOrder;
}