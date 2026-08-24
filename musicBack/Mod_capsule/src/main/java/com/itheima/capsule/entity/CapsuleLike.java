package com.itheima.capsule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 胶囊点赞实体类，对应表 capsule_like
 */
@Data
@TableName("capsule_like")
public class CapsuleLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer capsuleId;

    private Integer userId;

    private LocalDateTime createTime;
}
