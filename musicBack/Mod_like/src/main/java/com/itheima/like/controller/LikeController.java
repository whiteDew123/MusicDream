package com.itheima.like.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.domain.common.Result;
import com.itheima.like.entity.LikeList;
import com.itheima.like.entity.LikeMusic;
import com.itheima.like.service.LikeListService;
import com.itheima.like.service.LikeMusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    public Result<List<LikeMusic>> likedMusic(@RequestHeader("X-User-Id") Integer userId) {
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
                                 @RequestHeader("X-User-Id") Integer userId) {
        LikeMusic existing = likeMusicService.getOne(new LambdaQueryWrapper<LikeMusic>()
                .eq(LikeMusic::getUserId, userId)
                .eq(LikeMusic::getMusicId, musicId));
        if (existing != null) {
            return Result.fail("已收藏过该歌曲");
        }
        LikeMusic like = new LikeMusic();
        like.setUserId(userId);
        like.setMusicId(musicId);
        likeMusicService.save(like);
        return Result.success();
    }

    /**
     * 3. 移除收藏歌曲
     * <p>
     * DELETE /like/music/{musicId}
     */
    @DeleteMapping("/music/{musicId}")
    public Result<Void> removeMusic(@PathVariable Integer musicId,
                                    @RequestHeader("X-User-Id") Integer userId) {
        likeMusicService.remove(new LambdaQueryWrapper<LikeMusic>()
                .eq(LikeMusic::getUserId, userId)
                .eq(LikeMusic::getMusicId, musicId));
        return Result.success();
    }

    // ======================== 收藏歌单 ========================

    /**
     * 4. 查询当前用户收藏的歌单列表
     * <p>
     * GET /like/list
     */
    @GetMapping("/list")
    public Result<List<LikeList>> likedList(@RequestHeader("X-User-Id") Integer userId) {
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
                                @RequestHeader("X-User-Id") Integer userId) {
        LikeList existing = likeListService.getOne(new LambdaQueryWrapper<LikeList>()
                .eq(LikeList::getUserId, userId)
                .eq(LikeList::getListId, listId));
        if (existing != null) {
            return Result.fail("已收藏过该歌单");
        }
        LikeList like = new LikeList();
        like.setUserId(userId);
        like.setListId(listId);
        likeListService.save(like);
        return Result.success();
    }

    /**
     * 6. 移除收藏歌单
     * <p>
     * DELETE /like/list/{listId}
     */
    @DeleteMapping("/list/{listId}")
    public Result<Void> removeList(@PathVariable Integer listId,
                                   @RequestHeader("X-User-Id") Integer userId) {
        likeListService.remove(new LambdaQueryWrapper<LikeList>()
                .eq(LikeList::getUserId, userId)
                .eq(LikeList::getListId, listId));
        return Result.success();
    }
}