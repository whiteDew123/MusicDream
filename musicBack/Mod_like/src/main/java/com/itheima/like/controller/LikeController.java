package com.itheima.like.controller;

import com.itheima.domain.common.Result;
import com.itheima.like.entity.LikeList;
import com.itheima.like.entity.LikeMusic;
import com.itheima.like.service.LikeListService;
import com.itheima.like.service.LikeMusicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏接口
 * <p>
 * 网关路由：/api/like/** → StripPrefix=1 → /like/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/like")
public class LikeController {

    private final LikeMusicService likeMusicService;
    private final LikeListService likeListService;

    public LikeController(LikeMusicService likeMusicService,
                         LikeListService likeListService) {
        this.likeMusicService = likeMusicService;
        this.likeListService = likeListService;
    }

    // ======================== 收藏歌曲 ========================

    /**
     * 1. 查询当前用户收藏的歌曲列表
     * <p>
     * GET /like/music
     */
    @GetMapping("/music")
    public Result<List<LikeMusic>> likedMusic(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<LikeMusic> list = likeMusicService.getLikedMusic(userId);
        return Result.success(list);
    }

    /**
     * 2. 添加收藏歌曲
     * <p>
     * POST /like/music/{musicId}
     */
    @PostMapping("/music/{musicId}")
    public Result<Void> addMusic(@PathVariable Integer musicId,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (likeMusicService.isLiked(userId, musicId)) {
            return Result.error("已收藏过该歌曲");
        }
        likeMusicService.addLike(userId, musicId);
        return Result.success();
    }

    /**
     * 3. 移除收藏歌曲
     * <p>
     * DELETE /like/music/{musicId}
     */
    @DeleteMapping("/music/{musicId}")
    public Result<Void> removeMusic(@PathVariable Integer musicId,
                                    @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        likeMusicService.removeLike(userId, musicId);
        return Result.success();
    }

    // ======================== 收藏歌单 ========================

    /**
     * 4. 查询当前用户收藏的歌单列表
     * <p>
     * GET /like/list
     */
    @GetMapping("/list")
    public Result<List<LikeList>> likedList(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<LikeList> list = likeListService.getLikedList(userId);
        return Result.success(list);
    }

    /**
     * 5. 添加收藏歌单
     * <p>
     * POST /like/list/{listId}
     */
    @PostMapping("/list/{listId}")
    public Result<Void> addList(@PathVariable Integer listId,
                                @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (likeListService.isLiked(userId, listId)) {
            return Result.error("已收藏过该歌单");
        }
        likeListService.addLike(userId, listId);
        return Result.success();
    }

    /**
     * 6. 移除收藏歌单
     * <p>
     * DELETE /like/list/{listId}
     */
    @DeleteMapping("/list/{listId}")
    public Result<Void> removeList(@PathVariable Integer listId,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        likeListService.removeLike(userId, listId);
        return Result.success();
    }
}