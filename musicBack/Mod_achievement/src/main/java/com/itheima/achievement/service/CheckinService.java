package com.itheima.achievement.service;

import com.itheima.achievement.vo.CheckinResultVO;

/**
 * 每日打卡业务接口
 */
public interface CheckinService {

    /**
     * 用户每日打卡
     * <p>
     * 同一天重复打卡返回已打卡提示；连续打卡中断会重置 streak。
     */
    CheckinResultVO checkin(Integer userId);
}
