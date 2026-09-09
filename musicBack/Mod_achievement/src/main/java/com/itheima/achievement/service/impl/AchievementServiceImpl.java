package com.itheima.achievement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.achievement.entity.AchievementRule;
import com.itheima.achievement.entity.UserAchievement;
import com.itheima.achievement.entity.UserLevel;
import com.itheima.achievement.mapper.AchievementRuleMapper;
import com.itheima.achievement.mapper.UserAchievementMapper;
import com.itheima.achievement.mapper.UserLevelMapper;
import com.itheima.achievement.service.AchievementService;
import com.itheima.achievement.vo.BadgeVO;
import com.itheima.achievement.vo.LevelInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 听歌成就业务实现
 * <p>
 * 等级映射规则：每 100 积分一级，即 level = floor(points / 100) + 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    /** 每 100 积分升一级 */
    private static final int POINTS_PER_LEVEL = 100;

    /** 行为类型常量 */
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_LIKE = "LIKE";

    /** 规则类型常量 */
    private static final String TYPE_LISTEN_TOTAL = "LISTEN_TOTAL";
    private static final String TYPE_LIKE_TOTAL = "LIKE_TOTAL";

    private final AchievementRuleMapper ruleMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserLevelMapper userLevelMapper;

    @Override
    public List<BadgeVO> achievementWall(Integer userId) {
        // 1. 全部规则（按 tier、id 升序，铜→银→金，同档按 id）
        List<AchievementRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<AchievementRule>()
                .orderByAsc(AchievementRule::getTier)
                .orderByAsc(AchievementRule::getId));

        // 2. 当前用户所有进度记录
        Map<Integer, UserAchievement> progressMap = new HashMap<>();
        if (userId != null) {
            List<UserAchievement> records = userAchievementMapper.selectList(
                    new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));
            for (UserAchievement r : records) {
                progressMap.put(r.getRuleId(), r);
            }
        }

        // 3. 合并成 BadgeVO
        List<BadgeVO> result = new ArrayList<>(rules.size());
        for (AchievementRule rule : rules) {
            BadgeVO vo = new BadgeVO();
            vo.setRuleId(rule.getId());
            vo.setCode(rule.getCode());
            vo.setName(rule.getName());
            vo.setDescription(rule.getDescription());
            vo.setType(rule.getType());
            vo.setTarget(rule.getTarget());
            vo.setTier(rule.getTier());

            UserAchievement ua = progressMap.get(rule.getId());
            if (ua != null) {
                vo.setProgress(ua.getProgress());
                vo.setUnlocked(ua.getUnlocked() != null && ua.getUnlocked() == 1);
                vo.setUnlockTime(ua.getUnlockTime());
            } else {
                vo.setProgress(0);
                vo.setUnlocked(false);
                vo.setUnlockTime(null);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public LevelInfoVO levelInfo(Integer userId) {
        LevelInfoVO vo = new LevelInfoVO();
        vo.setUserId(userId);

        UserLevel level = getOrCreate(userId);
        vo.setLevel(level.getLevel());
        vo.setPoints(level.getPoints());
        vo.setCheckinStreak(level.getCheckinStreak());

        // 等级阈值计算
        int currentMin = (level.getLevel() - 1) * POINTS_PER_LEVEL;
        int nextMin = level.getLevel() * POINTS_PER_LEVEL;
        vo.setCurrentLevelMin(currentMin);
        vo.setNextLevelMin(nextMin);
        vo.setPointsToNext(Math.max(0, nextMin - level.getPoints()));

        // 今日是否已打卡
        LocalDate today = LocalDate.now();
        vo.setCheckedInToday(today.equals(level.getLastCheckinDate()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> trigger(Integer userId, String action, Integer musicId) {
        if (userId == null || action == null) {
            return Collections.emptyList();
        }
        String act = action.toUpperCase();
        log.info("成就触发判定: userId={}, action={}, musicId={}", userId, act, musicId);

        // 确保用户存在等级记录
        UserLevel level = getOrCreate(userId);

        // 按行为类型筛选需要判定的规则
        String ruleType;
        int progressDelta; // 进度增量（收藏类用绝对值重置，故增量仅用于播放类）
        switch (act) {
            case ACTION_PLAY:
                ruleType = TYPE_LISTEN_TOTAL;
                progressDelta = 1;
                break;
            case ACTION_LIKE:
                ruleType = TYPE_LIKE_TOTAL;
                progressDelta = 0; // 收藏类用实际表计数重置，不增量
                break;
            default:
                log.warn("未知 action: {}", action);
                return Collections.emptyList();
        }

        List<AchievementRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<AchievementRule>()
                .eq(AchievementRule::getType, ruleType)
                .orderByAsc(AchievementRule::getTarget));

        if (rules.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> unlockedNow = new ArrayList<>();
        for (AchievementRule rule : rules) {
            // 收藏类：每次重新查 like_music 计数作为进度；播放类：累加 +1
            int newProgress;
            if (ACTION_LIKE.equals(act)) {
                newProgress = countUserLikes(userId);
            } else {
                newProgress = bumpProgress(userId, rule, progressDelta);
            }

            if (newProgress >= rule.getTarget()) {
                boolean justUnlocked = markUnlockedIfPossible(userId, rule, newProgress);
                if (justUnlocked) {
                    unlockedNow.add(rule.getName());
                    // 加积分、升级
                    addPoints(level, rule.getPoints());
                }
            } else {
                // 未达目标，仅更新进度
                updateProgress(userId, rule, newProgress);
            }
        }

        // 持久化等级（积分/level 可能变化）
        level.setUpdateTime(LocalDateTime.now());
        userLevelMapper.updateById(level);
        return unlockedNow;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取或创建用户等级记录
     */
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
                // 并发插入兜底：再查一次
                level = userLevelMapper.selectById(userId);
                if (level == null) {
                    throw e;
                }
            }
        }
        return level;
    }

    /**
     * 播放类进度累加 +1（返回累加后的值）
     */
    private int bumpProgress(Integer userId, AchievementRule rule, int delta) {
        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getRuleId, rule.getId()));
        int newProgress;
        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setRuleId(rule.getId());
            ua.setProgress(delta);
            ua.setUnlocked(0);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.insert(ua);
            newProgress = delta;
        } else {
            // 已解锁的不再累加
            if (ua.getUnlocked() != null && ua.getUnlocked() == 1) {
                return ua.getProgress();
            }
            newProgress = ua.getProgress() + delta;
            ua.setProgress(newProgress);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.updateById(ua);
        }
        return newProgress;
    }

    /**
     * 仅更新进度（不解锁），用于未达目标场景
     */
    private void updateProgress(Integer userId, AchievementRule rule, int newProgress) {
        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getRuleId, rule.getId()));
        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setRuleId(rule.getId());
            ua.setProgress(newProgress);
            ua.setUnlocked(0);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.insert(ua);
        } else if (ua.getUnlocked() == null || ua.getUnlocked() == 0) {
            ua.setProgress(newProgress);
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.updateById(ua);
        }
    }

    /**
     * 达标后标记解锁（若已解锁则跳过）
     *
     * @return 是否本次新解锁
     */
    private boolean markUnlockedIfPossible(Integer userId, AchievementRule rule, int progress) {
        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getRuleId, rule.getId()));
        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setRuleId(rule.getId());
            ua.setProgress(progress);
            ua.setUnlocked(1);
            ua.setUnlockTime(LocalDateTime.now());
            ua.setUpdateTime(LocalDateTime.now());
            userAchievementMapper.insert(ua);
            return true;
        }
        if (ua.getUnlocked() != null && ua.getUnlocked() == 1) {
            return false; // 已解锁
        }
        ua.setProgress(progress);
        ua.setUnlocked(1);
        ua.setUnlockTime(LocalDateTime.now());
        ua.setUpdateTime(LocalDateTime.now());
        userAchievementMapper.updateById(ua);
        return true;
    }

    /**
     * 加积分并联动升级
     */
    private void addPoints(UserLevel level, Integer points) {
        if (points == null || points <= 0) {
            return;
        }
        int newPoints = level.getPoints() + points;
        level.setPoints(newPoints);
        level.setLevel(Math.floorDiv(newPoints, POINTS_PER_LEVEL) + 1);
    }

    /**
     * 查询用户收藏歌曲数（直接查 like_music 表）
     */
    private int countUserLikes(Integer userId) {
        // 复用 user_achievement 的查询语义太复杂，这里直接 count 表
        // 为避免循环依赖，用 BaseMapper.selectCount + LambdaQueryWrapper 不可行（UserAchievement 表不含 music）
        // 改为对 like_music 表原生 SQL —— 但本模块未引入该表 Mapper，改为通过 userAchievementMapper 反查 like_music 不可达
        // 折中：通过 userAchievementMapper 自定义 SQL 查 like_music 计数（见 mapper）
        return userAchievementMapper.countUserLikes(userId);
    }
}
