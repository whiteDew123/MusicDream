package com.itheima.like.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 歌曲点赞实体
 * <p>
 * 对应表 song_like，联合主键 (music_id, user_id)，幂等。
 */
@Data
@TableName("song_like")
public class SongLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer musicId;

    private Integer userId;

    private Date createTime;
}
