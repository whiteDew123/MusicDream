package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 音乐实体类，对应表 music
 */
@Data
@TableName("music")
public class Music implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer musicId;

    /** 歌手ID，对应 user.id */
    private Integer fromSinger;

    /** 歌曲名 */
    private String musicName;

    /** 音频文件URL */
    private String musicUrl;

    /** 状态: 0-正常, 1-用户锁定, 2-管理员锁定 */
    private Integer activation;

    /** 播放量 */
    private Integer listenNumb;

    /** 封面图片URL */
    private String imageUrl;

    /** 时长（秒） */
    private Integer timelength;

    /** 创建时间 */
    private LocalDate createTime;

    /** 标签（逗号分隔） */
    private String tags;

    /** 歌词文件URL */
    private String lyric;
}
