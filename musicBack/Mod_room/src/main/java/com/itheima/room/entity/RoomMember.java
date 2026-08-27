package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间成员实体类，对应表 room_member
 */
@Data
@TableName("room_member")
public class RoomMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID */
    private Long roomId;

    /** 用户ID */
    private Long userId;

    /** 角色：0-房主 1-成员 */
    private Integer role;

    /** 在线状态：0-离线 1-在线 */
    private Integer isOnline;

    /** 最近心跳时间 */
    private LocalDateTime lastHeartbeat;

    /** 加入时间 */
    private LocalDateTime joinTime;
}
