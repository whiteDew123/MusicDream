package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 收藏单曲表实体，对应表 like_music
 */
@Data
@TableName("like_music")
public class Mylike implements Serializable {

    /** 音乐ID（列 music_id） */
    @TableField("music_id")
    private Integer musicId;

    /** 用户ID（列 user_id） */
    @TableField("user_id")
    private Integer userId;
}