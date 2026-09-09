package com.itheima.songlist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 歌单实体
 * <p>
 * 对应表 song_list。username、isLike 为联表/计算字段，不入库。
 */
@Data
@TableName("song_list")
public class SongList implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 歌单名称 */
    private String name;

    /** 封面URL（列 pic） */
    @TableField("pic")
    private String pic;

    /** 简介（列 introduction） */
    @TableField("introduction")
    private String introduction;

    /** 风格（列 style） */
    @TableField("style")
    private String style;

    /** 创建者用户ID（列 user_id） */
    @TableField("user_id")
    private Integer userId;

    /** 创建时间 */
    private Date createDate;

    /** 创建者用户名（联表 user.username，不入库） */
    @TableField(exist = false)
    private String username;

    /** 当前用户是否已收藏：1=已收藏，null=未收藏（计算字段，不入库） */
    @TableField(exist = false)
    private Integer isLike;
}