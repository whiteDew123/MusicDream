package com.itheima.like.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户收藏歌单实体
 * <p>
 * 对应表 likelist。listName、listPic、username 为联表字段，不入库。
 */
@Data
@TableName("likelist")
public class LikeList implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID（列 user_id） */
    private Integer userId;

    /** 歌单ID（列 list_id） */
    private Integer listId;

    /** 歌单名称（联表 song_list.name，不入库） */
    @TableField(exist = false)
    private String listName;

    /** 歌单封面（联表 song_list.pic，不入库） */
    @TableField(exist = false)
    private String listPic;

    /** 创建者用户名（联表 user.username，不入库） */
    @TableField(exist = false)
    private String username;
}
