package com.itheima.songlist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.domain.common.Result;
import com.itheima.songlist.entity.LikeList;
import com.itheima.songlist.entity.ListMusic;
import com.itheima.songlist.entity.SongList;
import com.itheima.songlist.service.LikeListService;
import com.itheima.songlist.service.ListMusicService;
import com.itheima.songlist.service.SongListService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 歌单接口
 * <p>
 * 网关路由：/api/songList/** → StripPrefix=1 → /songList/**
 * <p>
 * 公开接口（/songList/public/**）在网关白名单中，无需登录即可访问；
 * 其余接口需携带 Authorization 头，网关解析后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/songList")
public class SongListController {

    private final SongListService songListService;
    private final ListMusicService listMusicService;
    private final LikeListService likeListService;

    public SongListController(SongListService songListService,
                             ListMusicService listMusicService,
                             LikeListService likeListService) {
        this.songListService = songListService;
        this.listMusicService = listMusicService;
        this.likeListService = likeListService;
    }

    // ======================== 公开接口（白名单） ========================

    /**
     * 1. 获取公开歌单列表
     * <p>
     * GET /songList/public/list
     * 已登录用户的 X-User-Id 头由网关注入（可选），用于标记收藏状态
     */
    @GetMapping("/public/list")
    public Result<List<SongList>> publicList(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        List<SongList> list = songListService.getPublicList(userId);
        return Result.success(list);
    }

    /**
     * 2. 获取歌单详情
     * <p>
     * GET /songList/public/detail/{id}
     */
    @GetMapping("/public/detail/{id}")
    public Result<SongList> publicDetail(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        SongList songList = songListService.getPublicDetail(id, userId);
        if (songList == null) {
            return Result.fail(404, "歌单不存在");
        }
        return Result.success(songList);
    }

    // ======================== 需登录接口 ========================

    /**
     * 3. 创建歌单
     * <p>
     * POST /songList
     */
    @PostMapping
    public Result<SongList> create(@RequestBody SongList songList,
                                   @RequestHeader("X-User-Id") Integer userId) {
        songList.setId(null);
        songList.setUserId(userId);
        songList.setCreateDate(new Date());
        songListService.save(songList);
        return Result.success("创建成功", songList);
    }

    /**
     * 4. 修改歌单
     * <p>
     * PUT /songList
     */
    @PutMapping
    public Result<Void> update(@RequestBody SongList songList,
                               @RequestHeader("X-User-Id") Integer userId) {
        SongList existing = songListService.getById(songList.getId());
        if (existing == null) {
            return Result.fail(404, "歌单不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.fail(403, "无权修改他人歌单");
        }
        songList.setUserId(userId);
        songListService.updateById(songList);
        return Result.success();
    }

    /**
     * 5. 删除歌单（同时清理关联歌曲和收藏记录）
     * <p>
     * DELETE /songList/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @RequestHeader("X-User-Id") Integer userId) {
        SongList existing = songListService.getById(id);
        if (existing == null) {
            return Result.fail(404, "歌单不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.fail(403, "无权删除他人歌单");
        }
        songListService.removeById(id);
        listMusicService.remove(new LambdaQueryWrapper<ListMusic>()
                .eq(ListMusic::getListId, id));
        likeListService.remove(new LambdaQueryWrapper<LikeList>()
                .eq(LikeList::getListId, id));
        return Result.success();
    }

    /**
     * 6. 添加歌曲到歌单
     * <p>
     * POST /songList/music
     * 请求体：{"listId": 1, "musicId": 10}
     */
    @PostMapping("/music")
    public Result<Void> addMusic(@RequestBody ListMusic listMusic,
                                 @RequestHeader("X-User-Id") Integer userId) {
        SongList songList = songListService.getById(listMusic.getListId());
        if (songList == null) {
            return Result.fail(404, "歌单不存在");
        }
        if (!songList.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作他人歌单");
        }
        ListMusic existing = listMusicService.getOne(new LambdaQueryWrapper<ListMusic>()
                .eq(ListMusic::getListId, listMusic.getListId())
                .eq(ListMusic::getMusicId, listMusic.getMusicId()));
        if (existing != null) {
            return Result.fail("歌曲已在歌单中");
        }
        listMusic.setId(null);
        listMusicService.save(listMusic);
        return Result.success();
    }

    /**
     * 7. 从歌单移除歌曲
     * <p>
     * DELETE /songList/music
     * 请求体：{"listId": 1, "musicId": 10}
     */
    @DeleteMapping("/music")
    public Result<Void> removeMusic(@RequestBody ListMusic listMusic,
                                    @RequestHeader("X-User-Id") Integer userId) {
        SongList songList = songListService.getById(listMusic.getListId());
        if (songList == null) {
            return Result.fail(404, "歌单不存在");
        }
        if (!songList.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作他人歌单");
        }
        listMusicService.remove(new LambdaQueryWrapper<ListMusic>()
                .eq(ListMusic::getListId, listMusic.getListId())
                .eq(ListMusic::getMusicId, listMusic.getMusicId()));
        return Result.success();
    }

    /**
     * 8. 收藏 / 取消收藏歌单（toggle）
     * <p>
     * POST /songList/like/{id}
     * 返回 data=true 表示收藏成功，data=false 表示已取消收藏
     */
    @PostMapping("/like/{id}")
    public Result<Boolean> toggleLike(@PathVariable Integer id,
                                      @RequestHeader("X-User-Id") Integer userId) {
        SongList songList = songListService.getById(id);
        if (songList == null) {
            return Result.fail(404, "歌单不存在");
        }
        LikeList existing = likeListService.getOne(new LambdaQueryWrapper<LikeList>()
                .eq(LikeList::getUserId, userId)
                .eq(LikeList::getListId, id));
        if (existing != null) {
            likeListService.removeById(existing.getId());
            return Result.success("取消收藏", false);
        }
        LikeList like = new LikeList();
        like.setUserId(userId);
        like.setListId(id);
        likeListService.save(like);
        return Result.success("收藏成功", true);
    }
}
