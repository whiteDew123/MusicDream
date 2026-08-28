package com.itheima.room.vo;

import lombok.Data;

/**
 * 房间成员 VO
 */
@Data
public class RoomMemberVO {

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String username;

    /** 用户头像 */
    private String imageUrl;

    /** 角色：0-房主 1-成员 */
    private Integer role;

    /** 在线状态：0-离线 1-在线 */
    private Integer isOnline;
}
