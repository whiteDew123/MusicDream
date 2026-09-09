package com.itheima.achievement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.achievement.entity.AchievementRule;
import com.itheima.achievement.entity.UserAchievement;
import com.itheima.achievement.entity.UserLevel;
import com.itheima.achievement.mapper.AchievementRuleMapper;
import com.itheima.achievement.mapper.UserAchievementMapper;
import com.itheima.achievement.mapper.UserLevelMapper;
import com.itheima.achievement.service.CheckinService;
import com.itheima.achievement.vo.CheckinResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 每日打卡业务实现
 * <p>
 * streak 规则：
 * - 今日已打卡 → 返回失败提示
 * - 上次打卡是昨天 → streak + 1
 * - 上次打卡是今天 → 不应发生（已被前置拦截）
 * - 其它（首次/中断）→ streak = 1
 * <p>
 * 每次打卡固定 +5 积分；连续达标的打卡类成就通过 trigger 逻辑联动解锁。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    /** 每次打卡固定奖励积分 */
    private static final int CHECKIN_POINTS = 5;

    /** 打卡成就类型 */
    private static final String TYPE_CHECKIN = "CHECKIN";

    private final UserLevelMapper userLevelMapper;
    private final AchievementRuleMapper ruleMapper;
    private final UserAchievementMapper userAchievementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResultVO checkin(Integer userId) {
        CheckinResultVO result = new CheckinResultVO();

        UserLevel level = getOrCreate(userId);
        LocalDate today = LocalDate.now();

        // 今日已打卡
        if (today.equals(level.getLastCheckinDate())) {
            result.setSuccess(false);
            result.setMessage("今日已打卡，连续 " + level.getCheckinStreak() + " 天");
            result.setCheckinStreak(level.getCheckinStreak());
            result.setPointsGained(0);
            result.setUnlockedAchievements(new ArrayList<>());
            return result;
        }

        // 计算 streak
        int newStreak;
        LocalDate last = level.getLastCheckinDate();
        if (last == null) {
            newStreak = 1;
        } else {
            long gap = ChronoUnit.DAYS.between(last, today);
            if (gap == 1) {
                newStreak = level.getCheckinStreak() + 1;
            } else if (gap <= 0) {
                // 同一天（理论已被前置拦截，兜底）
                newStreak = level.getCheckinStreak();
            } else {
                // 中断
                newStreak = 1;
            }
        }

        // 更新 user_level
        level.setCheckinStreak(newStreak);
        level.setLastCheckinDate(today);
        level.setPoints(level.getPoints() + CHECKIN_POINTS);
        // 等级联动：每 100 积分一级
        level.setLevel(Math.floorDiv(level.getPoints(), 100) + 1);
        level.setUpdateTime(LocalDateTime.now());
        userLevelMapper.updateById(level);

        // 判定 CHECKIN 类成就
        List<String> unlockedNow = evaluateCheckinAchievements(userId, newStreak, level);

        result.setSuccess(true);
        result.setMessage("打卡成功，连续 " + newStreak + " 天");
        result.setCheckinStreak(newStreak);
        result.setPointsGained(CHECKIN_POINTS);
        result.setUnlockedAchievements(unlockedNow);
        return result;
    }

    /**
     * 判定 CHECKIN 类规则是否达标
     */
    private List<String> evaluateCheckinAchievements(Integer userId, int streak, UserLevel level) {
        List<AchievementRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<AchievementRule>()
                .eq(AchievementRule::getType, TYPE_CHECKIN)
                .orderByAsc(AchievementRule::getTarget));

        List<String> unlockedNow = new ArrayList<>();
        for (AchievementRule rule : rules) {
            if (streak < rule.getTarget()) {
                // 未达标，仅更新进度
                upsertProgress(userId, rule.getId(), streak, false);
                continue;
            }
            boolean justUnlocked = upsertProgress(userId, rule.getId(), streak, true);
            if (justUnlocked) {
                unlockedNow.add(rule.getName());
                level.setPoints(level.getPoints() + rule.getPoints());
            }
        }
        // 积分可能因解锁再次变化，统一持久化一次
        level.setUpdateTime(LocalDateTime.now());
        userLevelMapper.updateById(level);
        return unlockedNow;
    }

    /**
     * 插入或更新进度；返回是否本次新解锁
     */
    private boolean upsertProgress(Integer userId, Integer ruleId, int progress, boolean unlocked) {
        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getRuleId, ruleId));
        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setRuleId(ruleId);
            ua.setProgress(progress);
            ua.setUnlocked(unlocked ? 1 : 0);
            ua.setUnlockTime(unlocked ? LocalDateTime.now() : null);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.insert(ua);
            return unlocked;
        }
        if (ua.getUnlocked() != null && ua.getUnlocked() == 1) {
            // 已解锁，仅更新进度
            ua.setProgress(progress);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.updateById(ua);
            return false;
        }
        ua.setProgress(progress);
        ua.setUnlocked(unlocked ? 1 : 0);
        ua.setUnlockTime(unlocked ? LocalDateTime.now() : null);
        ua.setUpdateTime(LocalDateTime.now());
        userAchievementMapper.updateById(ua);
        return unlocked;
    }

    private UserLevel getOrCreate(Integer userId) {
        UserLevel level = userLevelMapper.selectById(userId);
        if (level == null) {
            level = new UserLevel();
            level.setUserId(userId);
            level.setPoints(0);
            level.setLevel(1);
            level.setCheckinStreak(0);
            level.setLastCheckinDate(null);
            level.setUpdateTime(LocalDateTime.now());
            try {
                userLevelMapper.insert(level);
            } catch (Exception e) {
                level = userLevelMapper.selectById(userId);
                if (level == null) {
                    throw e;
                }
            }
        }
        return level;
    }
}
