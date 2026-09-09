package com.itheima.recognize.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 歌曲指纹实体
 */
@Data
@TableName("song_fingerprint")
public class SongFingerprint {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 歌曲ID，关联 music.music_id */
    private Integer musicId;

    /** 指纹哈希值 */
    private Long hashValue;

    /** 该指纹在歌曲中的时间偏移（秒） */
    private Double timeOffset;
}
