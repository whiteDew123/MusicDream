package com.itheima.songlist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 歌单-歌曲关联实体
 * <p>
 * 对应表 list_music，维护歌单与歌曲的多对多关系。
 * 联表字段（musicName 等）不入库，仅在查询歌曲列表时填充。
 */
@Data
@TableName("list_music")
public class ListMusic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 歌曲ID（列 music，复合主键之一） */
    @TableField("music")
    private Integer musicId;

    /** 歌单ID（列 listid，复合主键之一） */
    @TableField("listid")
    private Integer listId;

    /** 歌曲名称（联表 music.music_name，不入库） */
    @TableField(exist = false)
    private String musicName;

    /** 音频URL（联表 music.music_url，不入库） */
    @TableField(exist = false)
    private String musicUrl;

    /** 封面URL（联表 music.image_url，不入库） */
    @TableField(exist = false)
    private String imageUrl;

    /** 时长秒数（联表 music.timelength，不入库） */
    @TableField(exist = false)
    private Integer timelength;

    /** 播放量（联表 music.listen_numb，不入库） */
    @TableField(exist = false)
    private Integer listenNumb;

    /** 歌手ID（联表 music.from_singer，不入库） */
    @TableField(exist = false)
    private Integer singerId;

    /** 歌手名称（联表 user.username，不入库） */
    @TableField(exist = false)
    private String singerName;

    /** 歌词URL（联表 music.lyric，不入库） */
    @TableField(exist = false)
    private String lyric;
}