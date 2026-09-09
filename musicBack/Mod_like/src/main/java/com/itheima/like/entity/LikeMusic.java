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

    @TableField(exist = false)
    private Integer id;

    /** 用户ID（列 user_id） */
    @TableField("user_id")
    private Integer userId;

    /** 歌曲ID（列 music_id） */
    @TableField("music_id")
    private Integer musicId;

    /** 收藏时间（联表 music.create_time，不入库） */
    @TableField(exist = false)
    private Date createDate;

    /** 歌曲名称（联表 music.music_name，不入库） */
    @TableField(exist = false)
    private String musicName;

    /** 歌曲封面（联表 music.image_url，不入库） */
    @TableField(exist = false)
    private String musicPic;

    /** 歌手名称（联表 user.username，不入库） */
    @TableField(exist = false)
    private String singerName;

    /** 音频URL（联表 music.music_url，不入库） */
    @TableField(exist = false)
    private String musicUrl;

    /** 时长（秒，联表 music.timelength，不入库） */
    @TableField(exist = false)
    private Integer timelength;

    /** 歌词URL（联表 music.lyric，不入库） */
    @TableField(exist = false)
    private String lyric;

    /** 播放量（联表 music.listen_numb，不入库） */
    @TableField(exist = false)
    private Integer listenNumb;

    /** 封面URL（联表 music.image_url，不入库，前端统一用 imageUrl） */
    @TableField(exist = false)
    private String imageUrl;
}