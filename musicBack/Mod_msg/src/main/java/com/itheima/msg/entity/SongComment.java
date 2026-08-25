package com.itheima.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 歌曲评论实体
 * <p>
 * 对应表 song_comment。avatar/username/musicName 为联表字段，不入库。
 */
@Data
@TableName("song_comment")
public class SongComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer musicId;

    private Integer userId;

    private String content;

    private Long parentId;

    private Integer toUserId;

    private Integer likes;

    private Integer isTop;

    private Date createTime;

    /** 评论用户昵称（联表 user.username，不入库） */
    @TableField(exist = false)
    private String username;

    /** 评论用户头像（联表 user.image_url，不入库） */
    @TableField(exist = false)
    private String avatar;

    /** 当前用户是否已点赞该评论（不入库，运行时计算） */
    @TableField(exist = false)
    private Boolean liked;
}
