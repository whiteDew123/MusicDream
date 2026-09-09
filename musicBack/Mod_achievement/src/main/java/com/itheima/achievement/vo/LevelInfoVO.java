package com.itheima.achievement.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户等级信息 VO
 */
@Data
public class LevelInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Integer userId;

    /** 当前等级 */
    private Integer level;

    /** 累计积分 */
    private Integer points;

    /** 当前等级起始积分阈值 */
    private Integer currentLevelMin;

    /** 下一等级起始积分阈值（无下一级则与 currentLevelMin 相同） */
    private Integer nextLevelMin;

    /** 距下一级还需积分（无下一级为 0） */
    private Integer pointsToNext;

    /** 连续打卡天数 */
    private Integer checkinStreak;

    /** 今日是否已打卡 */
    private Boolean checkedInToday;
}
