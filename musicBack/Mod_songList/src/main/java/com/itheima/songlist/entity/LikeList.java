package com.itheima.songlist.entity;

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
 * 对应表 likelist，记录用户与歌单的收藏关系。
 */
@Data
@TableName("likelist")
public class LikeList implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID（列 userId，复合主键之一） */
    private Integer userId;

    /** 歌单ID（列 listid，复合主键之一） */
    @TableField("listid")
    private Integer listId;
}