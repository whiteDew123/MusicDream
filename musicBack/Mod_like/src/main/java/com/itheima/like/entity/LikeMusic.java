package com.itheima.like.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户收藏歌曲实体
 * <p>
 * 对应表 like_music。musicName、singerName 为联表字段，不入库。
 */
@Data
@TableName("like_music")
public class LikeMusic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID（列 user_id） */
    private Integer userId;

    /** 歌曲ID（列 music_id） */
    private Integer musicId;

    /** 收藏时间 */
    private Date createDate;

    /** 歌曲名称（联表 music.name，不入库） */
    @TableField(exist = false)
    private String musicName;

    /** 歌曲封面（联表 music.pic，不入库） */
    @TableField(exist = false)
    private String musicPic;

    /** 歌手名称（联表 singer.name，不入库） */
    @TableField(exist = false)
    private String singerName;
}
