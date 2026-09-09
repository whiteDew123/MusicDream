package com.itheima.room.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间列表项 / 详情基础 VO
 */
@Data
public class RoomVO {

    /** 房间ID */
    private Long id;

    /** 房间名称 */
    private String name;

    /** 房主用户ID */
    private Long ownerId;

    /** 房主昵称 */
    private String ownerName;

    /** 人数上限 */
    private Integer maxMembers;

    /** 当前成员数 */
    private Long memberCount;

    /** 房间封面URL */
    private String cover;

    /** 是否公开：0-私密 1-公开 */
    private Integer isPublic;

    /** 邀请码 */
    private String inviteCode;

    /** 状态：0-空闲 1-播放中 2-已结束 */
    private Integer status;

    /** 当前歌曲ID */
    private Long currentMusicId;

    /** 当前播放进度（秒） */
    private Double currentProgress;

    /** 是否播放中：0-暂停 1-播放 */
    private Integer isPlaying;

    /** 播放模式：0-循环播放 1-播放完毕停止 */
    private Integer playMode;

    /** 邀请码有效期（小时） */
    private Integer inviteExpireHours;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 当前用户是否为房主 */
    private Boolean isOwner;

    /** 当前用户是否为成员 */
    private Boolean isMember;
}
