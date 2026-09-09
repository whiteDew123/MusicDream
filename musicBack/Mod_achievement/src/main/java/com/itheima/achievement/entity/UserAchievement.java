package com.itheima.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户成就进度实体，对应表 user_achievement
 */
@Data
@TableName("user_achievement")
public class UserAchievement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 用户ID */
    private Integer userId;

    /** 规则ID */
    private Integer ruleId;

    /** 当前进度 */
    private Integer progress;

    /** 是否解锁：0否 / 1是 */
    private Integer unlocked;

    /** 解锁时间 */
    private LocalDateTime unlockTime;

    /** 进度更新时间 */
    private LocalDateTime updateTime;
}
