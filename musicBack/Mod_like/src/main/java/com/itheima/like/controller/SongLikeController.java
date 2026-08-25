package com.itheima.like.controller;

import com.itheima.domain.common.Result;
import com.itheima.like.service.SongLikeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 歌曲点赞接口
 * <p>
 * 网关路由：/api/like/song/** → StripPrefix=1 → /like/song/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 * 决策：❤️ 点赞为表态，与 ⭐ 收藏（/like/music/{musicId}）分离。
 */
@RestController
@RequestMapping("/like/song")
public class SongLikeController {

    private final SongLikeService songLikeService;

    public SongLikeController(SongLikeService songLikeService) {
        this.songLikeService = songLikeService;
    }

    /**
     * 切换点赞状态（已赞→取消，未赞→点赞）
     * <p>
     * POST /like/song/{musicId}
     */
    @PostMapping("/{musicId}")
    public Result<Map<String, Object>> toggleLike(
            @PathVariable Integer musicId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Map<String, Object> result = songLikeService.toggleLike(musicId, userId);
        return Result.success(result);
    }

    /**
     * 查询点赞状态 + 点赞数
     * <p>
     * GET /like/song/{musicId}/status
     */
    @GetMapping("/{musicId}/status")
    public Result<Map<String, Object>> getLikeStatus(
            @PathVariable Integer musicId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        Map<String, Object> result = songLikeService.getLikeStatus(musicId, userId);
        return Result.success(result);
    }

    /**
     * 统一查询歌曲交互统计（点赞状态 + 点赞数 + 评论数 + 分享数）
     * <p>
     * GET /like/song/{musicId}/stats
     * <p>
     * 一次请求返回全部数据，前端高效获取交互状态。
     */
    @GetMapping("/{musicId}/stats")
    public Result<Map<String, Object>> getMusicStats(
            @PathVariable Integer musicId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        Map<String, Object> result = songLikeService.getMusicStats(musicId, userId);
        return Result.success(result);
    }
}
