package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间统计实体，对应表 room_stats
 */
@Data
@TableName("room_stats")
public class RoomStats implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** 房间ID（主键，关联 room.id） */
    @TableId
    private Long roomId;

    /** 历史峰值在线人数 */
    private Integer peakOnline;

    /** 累计观看分钟数 */
    private Long totalWatchMinutes;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
