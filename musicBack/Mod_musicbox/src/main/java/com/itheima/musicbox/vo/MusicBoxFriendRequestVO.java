package com.itheima.musicbox.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MusicBoxFriendRequestVO {
    private Integer id;
    private Integer boxId;
    private String boxTitle;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private Integer receiverId;
    private String message;
    private Integer status;
    private LocalDateTime createTime;
}