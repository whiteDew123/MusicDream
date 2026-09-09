package com.itheima.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 成就规则实体，对应表 achievement_rule
 */
@Data
@TableName("achievement_rule")
public class AchievementRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 规则编码，如 LISTEN_TOTAL_100 */
    private String code;

    /** 展示名，如 百听不厌 */
    private String name;

    /** 描述 */
    private String description;

    /** 类型：LISTEN_TOTAL / CHECKIN / LIKE_TOTAL */
    private String type;

    /** 目标阈值 */
    private Integer target;

    /** 解锁奖励积分 */
    private Integer points;

    /** 徽章档位：1铜 / 2银 / 3金 */
    private Integer tier;
}
