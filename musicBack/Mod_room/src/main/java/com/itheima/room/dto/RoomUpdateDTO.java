package com.itheima.room.dto;

import lombok.Data;

/**
 * 修改房间请求体（仅房主）
 * <p>
 * 字段可空：为 null 的字段表示不修改。
 */
@Data
public class RoomUpdateDTO {

    /** 房间ID */
    private Long id;

    /** 房间名称 */
    private String name;

    /** 人数上限（范围 2-5） */
    private Integer maxMembers;

    /** 是否公开 */
    private Boolean isPublic;

    /** 邀请码有效期（小时），1-12 */
    private Integer inviteExpireHours;

    /** 房间封面URL */
    private String cover;

    /** 播放模式：0-循环播放 1-播放完毕停止 */
    private Integer playMode;
}
