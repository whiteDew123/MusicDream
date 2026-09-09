package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间实体类，对应表 room
 * <p>
 * 一起听·播放室的房间主表，含房主、人数、邀请码、播放状态等。
 */
@Data
@TableName("room")
public class Room implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 房间ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间名称 */
    private String name;

    /** 房主用户ID */
    private Long ownerId;

    /** 人数上限 */
    private Integer maxMembers;

    /** 房间封面URL（P3 预留） */
    private String cover;

    /** 是否公开：0-私密 1-公开 */
    private Integer isPublic;

    /** 邀请码（8 位，唯一） */
    private String inviteCode;

    /** 状态：0-空闲 1-播放中 2-已结束 */
    private Integer status;

    /** 当前歌曲ID */
    private Long currentMusicId;

    /** 当前进度（秒） */
    private Double currentProgress;

    /** 是否播放中：0-暂停 1-播放 */
    private Integer isPlaying;

    /** 播放模式：0-循环播放 1-播放完毕停止 */
    private Integer playMode;

    /** 邀请码有效期（小时） */
    private Integer inviteExpireHours;

    /** 创建时间 */
    private LocalDateTime createTime;
}
