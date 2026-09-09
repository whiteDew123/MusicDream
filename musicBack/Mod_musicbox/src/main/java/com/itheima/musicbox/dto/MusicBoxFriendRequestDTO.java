package com.itheima.musicbox.dto;

import lombok.Data;

@Data
public class MusicBoxFriendRequestDTO {
    private Integer receiverId;
    private String message;
}