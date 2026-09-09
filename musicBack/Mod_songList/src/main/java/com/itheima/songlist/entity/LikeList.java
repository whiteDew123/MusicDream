package com.itheima.songlist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户收藏歌单实体
 * <p>
 * 对应表 likelist，记录用户与歌单的收藏关系。
 */
@Data
@TableName("likelist")
public class LikeList implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Integer id;

    /** 用户ID（列 user_id） */
    @TableField("user_id")
    private Integer userId;

    /** 歌单ID（列 list_id） */
    @TableField("list_id")
    private Integer listId;
}