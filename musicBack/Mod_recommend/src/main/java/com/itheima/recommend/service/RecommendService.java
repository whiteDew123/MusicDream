package com.itheima.recommend.service;

import com.itheima.recommend.vo.ArtistDetailVO;
import com.itheima.recommend.vo.ArtistVO;
import com.itheima.recommend.vo.MusicVO;
import com.itheima.recommend.vo.SongDetailVO;

import java.util.List;

/**
 * 歌曲推荐搜索模块业务接口
 */
public interface RecommendService {

    /**
     * 推荐歌曲
     */
    List<MusicVO> recommendSongs(Integer userId, Integer limit);

    /**
     * 歌曲排行
     */
    List<MusicVO> rankSongs(Integer limit);

    /**
     * 歌曲搜索
     */
    List<MusicVO> searchSongs(String keyword, Integer page, Integer size);

    /**
     * 歌曲详情
     */
    SongDetailVO songDetail(Integer musicId);

    /**
     * 推荐歌手
     */
    List<ArtistVO> recommendArtists(Integer limit);

    /**
     * 歌手详情
     */
    ArtistDetailVO artistDetail(Integer artistId);

    /**
     * 清理推荐模块相关缓存（歌曲发布、审核通过、删除后调用）
     */
    void evictRecommendCache();
}
