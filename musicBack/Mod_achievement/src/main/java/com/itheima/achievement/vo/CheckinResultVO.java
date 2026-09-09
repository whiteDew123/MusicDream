package com.itheima.achievement.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 打卡结果 VO
 */
@Data
public class CheckinResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否打卡成功（今日已打卡则 false） */
    private Boolean success;

    /** 提示信息 */
    private String message;

    /** 本次打卡后连续天数 */
    private Integer checkinStreak;

    /** 本次打卡获得积分 */
    private Integer pointsGained;

    /** 本次打卡新解锁的成就名列表 */
    private List<String> unlockedAchievements;
}
