package com.itheima.achievement.service;

import com.itheima.achievement.vo.BadgeVO;
import com.itheima.achievement.vo.LevelInfoVO;

import java.util.List;

/**
 * 听歌成就模块业务接口
 */
public interface AchievementService {

    /**
     * 成就墙：返回全部规则 + 当前用户进度/解锁状态
     */
    List<BadgeVO> achievementWall(Integer userId);

    /**
     * 用户等级信息：等级、积分、距下一级进度、打卡信息
     */
    LevelInfoVO levelInfo(Integer userId);

    /**
     * 触发判定：监听播放/收藏事件
     *
     * @param action  PLAY / LIKE
     * @param musicId 仅作日志用，可空
     * @return 本次触发新解锁的成就名列表（无则空）
     */
    List<String> trigger(Integer userId, String action, Integer musicId);
}
