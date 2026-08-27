package com.itheima.room.dto;

import lombok.Data;

/**
 * 创建房间请求体
 */
@Data
public class RoomCreateDTO {

    /** 房间名称（必填） */
    private String name;

    /** 人数上限（默认 5，范围 2-5） */
    private Integer maxMembers;

    /** 是否公开（默认 false 私密） */
    private Boolean isPublic;

    /** 邀请码有效期（小时），范围 1-12（默认 6） */
    private Integer inviteExpireHours;

    /** 播放模式：0-循环播放 1-播放完毕停止（默认 0） */
    private Integer playMode;
}
