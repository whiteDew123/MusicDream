package com.itheima.recommend.controller;

import com.itheima.domain.common.Result;
import com.itheima.recommend.service.RecommendService;
import com.itheima.recommend.vo.ArtistDetailVO;
import com.itheima.recommend.vo.ArtistVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 歌手推荐、详情接口
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArtistController {

    private final RecommendService recommendService;

    /**
     * 推荐歌手
     */
    @GetMapping("/music/recommend/artists")
    public Result<List<ArtistVO>> recommendArtists(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(recommendService.recommendArtists(limit));
    }

    /**
     * 歌手详情
     */
    @GetMapping("/artists/{artistId}")
    public Result<ArtistDetailVO> artistDetail(@PathVariable Integer artistId) {
        ArtistDetailVO detail = recommendService.artistDetail(artistId);
        return detail == null ? Result.error(404, "歌手不存在") : Result.success(detail);
    }
}
