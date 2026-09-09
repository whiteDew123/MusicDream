package com.itheima.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户等级/积分/打卡实体，对应表 user_level
 */
@Data
@TableName("user_level")
public class UserLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID（主键） */
    @TableId(type = IdType.INPUT)
    private Integer userId;

    /** 累计积分 */
    private Integer points;

    /** 当前等级 */
    private Integer level;

    /** 连续打卡天数 */
    private Integer checkinStreak;

    /** 上次打卡日 */
    private LocalDate lastCheckinDate;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
