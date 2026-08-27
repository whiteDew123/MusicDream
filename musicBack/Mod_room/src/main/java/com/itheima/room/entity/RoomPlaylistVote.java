package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 切歌投票实体类，对应表 room_playlist_vote
 * <p>
 * 同一用户对同一首歌重复投票由唯一约束 (room_id, music_id, user_id) 保证。
 */
@Data
@TableName("room_playlist_vote")
public class RoomPlaylistVote implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID */
    private Long roomId;

    /** 目标歌曲ID */
    private Long musicId;

    /** 投票人ID */
    private Long userId;

    /** 投票发起时间，用于 30 秒超时判断 */
    private LocalDateTime createdAt;
}
