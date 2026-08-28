package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间歌单实体类，对应表 room_playlist
 */
@Data
@TableName("room_playlist")
public class RoomPlaylist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID */
    private Long roomId;

    /** 歌曲ID */
    private Long musicId;

    /** 添加者用户ID */
    private Long addedBy;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0-待播 1-播放中 2-已播 */
    private Integer status;

    /** 添加时间 */
    private LocalDateTime addTime;
}
