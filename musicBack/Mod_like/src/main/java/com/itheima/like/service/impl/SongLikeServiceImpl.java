package com.itheima.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.like.entity.SongLike;
import com.itheima.like.mapper.MusicStatsMapper;
import com.itheima.like.mapper.SongLikeMapper;
import com.itheima.like.service.SongLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class SongLikeServiceImpl extends ServiceImpl<SongLikeMapper, SongLike> implements SongLikeService {

    private final MusicStatsMapper musicStatsMapper;

    public SongLikeServiceImpl(MusicStatsMapper musicStatsMapper) {
        this.musicStatsMapper = musicStatsMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Integer musicId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int existing;
        try {
            existing = baseMapper.countByMusicAndUser(musicId, userId);
        } catch (Exception e) {
            // song_like 表不存在等异常
            result.put("liked", false);
            result.put("likesCount", 0);
            return result;
        }

        boolean liked;
        if (existing > 0) {
            // 已赞 → 取消
            baseMapper.deleteLike(musicId, userId);
            baseMapper.decrLikeCount(musicId);
            liked = false;
        } else {
            // 未赞 → 点赞
            baseMapper.insertLike(musicId, userId);
            baseMapper.incrLikeCount(musicId);
            liked = true;
        }

        int likesCount = baseMapper.countByMusicId(musicId);
        result.put("liked", liked);
        result.put("likesCount", likesCount);
        return result;
    }

    @Override
    public Map<String, Object> getLikeStatus(Integer musicId, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        boolean liked = false;
        if (userId != null) {
            int cnt = baseMapper.countByMusicAndUser(musicId, userId);
            liked = cnt > 0;
        }
        int likesCount = baseMapper.countByMusicId(musicId);
        result.put("liked", liked);
        result.put("likesCount", likesCount);
        return result;
    }

    @Override
    public Map<String, Object> getMusicStats(Integer musicId, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 点赞状态
        boolean liked = false;
        if (userId != null) {
            try {
                int cnt = baseMapper.countByMusicAndUser(musicId, userId);
                liked = cnt > 0;
            } catch (Exception e) {
                // 表不存在等异常时降级处理
            }
        }

        // 2. 从 music 表冗余列读取统计数
        int likesCount = 0;
        int commentCount = 0;
        int shareCount = 0;
        try {
            Map<String, Object> stats = musicStatsMapper.selectStatsByMusicId(musicId);
            if (stats != null) {
                likesCount = ((Number) stats.getOrDefault("likesCount", 0)).intValue();
                commentCount = ((Number) stats.getOrDefault("commentCount", 0)).intValue();
                shareCount = ((Number) stats.getOrDefault("shareCount", 0)).intValue();
            }
        } catch (Exception e) {
            // music 表可能缺少冗余列，降级返回 0
        }

        result.put("liked", liked);
        result.put("likesCount", likesCount);
        result.put("commentCount", commentCount);
        result.put("shareCount", shareCount);
        return result;
    }
}
