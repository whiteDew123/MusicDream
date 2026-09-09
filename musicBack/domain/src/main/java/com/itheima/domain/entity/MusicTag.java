package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 歌曲-标签关联实体，对应表 music_tag
 */
@Data
@TableName("music_tag")
public class MusicTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 歌曲ID（music.music_id） */
    private Integer musicId;

    /** 标签ID（tag.tag_id） */
    private Integer tagId;
}