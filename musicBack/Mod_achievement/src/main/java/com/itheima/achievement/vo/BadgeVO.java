package com.itheima.achievement.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成就墙单个徽章 VO（规则 + 当前用户进度合并）
 */
@Data
public class BadgeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Integer ruleId;

    /** 规则编码 */
    private String code;

    /** 展示名 */
    private String name;

    /** 描述 */
    private String description;

    /** 类型 */
    private String type;

    /** 目标阈值 */
    private Integer target;

    /** 档位：1铜/2银/3金 */
    private Integer tier;

    /** 当前进度 */
    private Integer progress;

    /** 是否解锁 */
    private Boolean unlocked;

    /** 解锁时间 */
    private LocalDateTime unlockTime;
}
