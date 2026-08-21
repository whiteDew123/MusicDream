package com.itheima.recommend.controller;

import com.itheima.domain.common.Result;
import com.itheima.recommend.service.RecommendService;
import com.itheima.recommend.vo.MusicVO;
import com.itheima.recommend.vo.SongDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 歌曲推荐、排行、搜索、详情接口
 */
@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final RecommendService recommendService;

    /**
     * 推荐歌曲
     */
    @GetMapping("/recommend/songs")
    public Result<List<MusicVO>> recommendSongs(@RequestParam(required = false) Integer userId,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(recommendService.recommendSongs(userId, limit));
    }

    /**
     * 歌曲排行
     */
    @GetMapping("/rank")
    public Result<List<MusicVO>> rankSongs(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(recommendService.rankSongs(limit));
    }

    /**
     * 歌曲搜索
     */
    @GetMapping("/search")
    public Result<List<MusicVO>> searchSongs(@RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(recommendService.searchSongs(keyword, page, size));
    }

    /**
     * 歌曲详情
     */
    @GetMapping("/{musicId}")
    public Result<SongDetailVO> songDetail(@PathVariable Integer musicId) {
        SongDetailVO detail = recommendService.songDetail(musicId);
        return detail == null ? Result.error(404, "歌曲不存在") : Result.success(detail);
    }
}