package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成员进出会话实体，对应表 room_member_session
 * 每次进入房间一行，离开时回填 leave_time + duration_sec
 */
@Data
@TableName("room_member_session")
public class RoomMemberSession implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;
    private Long userId;

    /** 进房时间 */
    private LocalDateTime enterTime;

    /** 离开时间（可为 NULL 表示当前在线） */
    private LocalDateTime leaveTime;

    /** 停留秒数（leave 时回填） */
    private Integer durationSec;
}
