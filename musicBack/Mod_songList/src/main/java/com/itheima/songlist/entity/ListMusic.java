package com.itheima.songlist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 歌单-歌曲关联实体
 * <p>
 * 对应表 list_music，维护歌单与歌曲的多对多关系。
 */
@Data
@TableName("list_music")
public class ListMusic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 歌单ID（列 list_id） */
    private Integer listId;

    /** 歌曲ID（列 music_id） */
    private Integer musicId;
}
