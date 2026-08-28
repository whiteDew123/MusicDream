package com.itheima.musicbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("music_box_friend_request")
public class MusicBoxFriendRequest {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer boxId;
    private Integer senderId;
    private Integer receiverId;
    private String message;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}