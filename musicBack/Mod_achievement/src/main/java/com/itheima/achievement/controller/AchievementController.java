package com.itheima.achievement.controller;

import com.itheima.achievement.service.AchievementService;
import com.itheima.achievement.service.CheckinService;
import com.itheima.achievement.vo.BadgeVO;
import com.itheima.achievement.vo.CheckinResultVO;
import com.itheima.achievement.vo.LevelInfoVO;
import com.itheima.domain.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 听歌成就 / 等级 / 打卡接口
 * <p>
 * 网关路由：/api/achievement/** → StripPrefix=1 → /achievement/**
 * <p>
 * 除 /trigger 外均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@Slf4j
@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final CheckinService checkinService;

    /**
     * 1. 成就墙：全部规则 + 当前用户进度/解锁状态
     * <p>
     * GET /achievement/wall
     */
    @GetMapping("/wall")
    public Result<List<BadgeVO>> wall(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(achievementService.achievementWall(userId));
    }

    /**
     * 2. 用户等级信息：等级、积分、距下一级进度、打卡信息
     * <p>
     * GET /achievement/level
     */
    @GetMapping("/level")
    public Result<LevelInfoVO> level(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(achievementService.levelInfo(userId));
    }

    /**
     * 3. 每日打卡
     * <p>
     * POST /achievement/checkin
     */
    @PostMapping("/checkin")
    public Result<CheckinResultVO> checkin(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(checkinService.checkin(userId));
    }

    /**
     * 4. 内部触发判定接口：播放/收藏事件回调
     * <p>
     * 前端在播放或收藏时同步调用，触发成就判定。
     * <p>
     * POST /achievement/trigger?action=PLAY&musicId=123
     * POST /achievement/trigger?action=LIKE&musicId=123
     */
    @PostMapping("/trigger")
    public Result<List<String>> trigger(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam String action,
            @RequestParam(required = false) Integer musicId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<String> unlocked = achievementService.trigger(userId, action, musicId);
        return Result.success(unlocked);
    }
}
