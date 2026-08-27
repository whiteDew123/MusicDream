package com.itheima.recommend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.Mylike;
import com.itheima.domain.entity.User;
import com.itheima.recommend.mapper.MusicMapper;
import com.itheima.recommend.mapper.MylikeMapper;
import com.itheima.recommend.mapper.UserMapper;
import com.itheima.recommend.service.RecommendService;
import com.itheima.recommend.vo.ArtistDetailVO;
import com.itheima.recommend.vo.ArtistVO;
import com.itheima.recommend.vo.MusicVO;
import com.itheima.recommend.vo.SongDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 歌曲推荐搜索模块业务实现
 *
 * <p>使用 MyBatis Plus 条件构造器进行查询，使用 Redis 缓存热点数据。
 * Redis 不可用时自动降级为直接查询数据库。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final MusicMapper musicMapper;
    private final UserMapper userMapper;
    private final MylikeMapper mylikeMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<MusicVO> recommendSongs(Integer userId, Integer limit) {
        int size = normalizeLimit(limit);
        String cacheKey = "recommend:songs:" + safe(userId) + ":" + size;

        List<MusicVO> cachedList = getCachedList(cacheKey, MusicVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<Music> result;
        if (userId != null) {
            result = recommendByUserLikes(userId, size);
        } else {
            result = topSongs(size);
        }

        List<MusicVO> voList = result.stream()
                .map(this::toMusicVO)
                .collect(Collectors.toList());

        setCache(cacheKey, JSON.toJSONString(voList));
        return voList;
    }

    @Override
    public List<MusicVO> rankSongs(Integer limit) {
        int size = normalizeLimit(limit);
        String cacheKey = "music:rank:play:" + size;

        List<MusicVO> cachedList = getCachedList(cacheKey, MusicVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<Music> songs = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getListenNumb)
                .last("LIMIT " + size));

        List<MusicVO> voList = songs.stream()
                .map(this::toMusicVO)
                .collect(Collectors.toList());

        setCache(cacheKey, JSON.toJSONString(voList));
        return voList;
    }

    @Override
    public List<MusicVO> rankSongsByPlayTime(Integer limit) {
        int size = normalizeLimit(limit);
        String cacheKey = "music:rank:time:" + size;

        List<MusicVO> cachedList = getCachedList(cacheKey, MusicVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<Music> songs = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getCreateTime)
                .last("LIMIT " + size));

        List<MusicVO> voList = songs.stream()
                .map(this::toMusicVO)
                .collect(Collectors.toList());

        setCache(cacheKey, JSON.toJSONString(voList));
        return voList;
    }

    @Override
    public List<MusicVO> searchSongs(String keyword, Integer page, Integer size) {
        int current = page == null || page < 1 ? 1 : page;
        int pageSize = normalizeLimit(size);
        String safeKeyword = keyword == null ? "" : keyword.trim();

        String cacheKey = "music:search:" + safeKeyword + ":" + current + ":" + pageSize;
        List<MusicVO> cachedList = getCachedList(cacheKey, MusicVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<Music>()
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getListenNumb);

        if (StringUtils.hasText(safeKeyword)) {
            wrapper.and(w -> w
                    .like(Music::getMusicName, safeKeyword)
                    .or()
                    .like(Music::getTags, safeKeyword));
        }

        wrapper.last("LIMIT " + (current - 1) * pageSize + ", " + pageSize);

        List<MusicVO> voList = musicMapper.selectList(wrapper).stream()
                .map(this::toMusicVO)
                .collect(Collectors.toList());

        setCache(cacheKey, JSON.toJSONString(voList));
        return voList;
    }

    @Override
    public SongDetailVO songDetail(Integer musicId) {
        if (musicId == null) {
            return null;
        }
        String cacheKey = "music:detail:" + musicId;

        SongDetailVO cached = getCachedObject(cacheKey, SongDetailVO.class);
        if (cached != null) {
            return cached;
        }

        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return null;
        }

        SongDetailVO vo = new SongDetailVO();
        vo.setMusicId(music.getMusicId());
        vo.setFromSinger(music.getFromSinger());
        vo.setMusicName(music.getMusicName());
        vo.setMusicUrl(music.getMusicUrl());
        vo.setActivation(music.getActivation());
        vo.setListenNumb(music.getListenNumb());
        vo.setImageUrl(music.getImageUrl());
        vo.setTimelength(music.getTimelength());
        vo.setCreateTime(music.getCreateTime());
        vo.setTags(music.getTags());
        vo.setLyric(music.getLyric());

        User singer = music.getFromSinger() == null ? null : userMapper.selectById(music.getFromSinger());
        if (singer != null) {
            vo.setSingerName(singer.getUsername());
            vo.setSingerImageUrl(singer.getImageUrl());
            vo.setSingerAbout(singer.getAbout());
        }

        setCache(cacheKey, JSON.toJSONString(vo));
        return vo;
    }

    @Override
    public List<ArtistVO> recommendArtists(Integer limit) {
        int size = normalizeLimit(limit);
        String cacheKey = "recommend:artists:" + size;

        List<ArtistVO> cachedList = getCachedList(cacheKey, ArtistVO.class);
        if (cachedList != null) {
            return cachedList;
        }

        List<User> singers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 1)
                .eq(User::getActivation, 0));

        List<ArtistVO> result = singers.stream()
                .map(singer -> {
                    ArtistVO vo = new ArtistVO();
                    vo.setId(singer.getId());
                    vo.setUsername(singer.getUsername());
                    vo.setImageUrl(singer.getImageUrl());
                    vo.setAbout(singer.getAbout());

                    List<Music> songs = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                            .eq(Music::getFromSinger, singer.getId())
                            .eq(Music::getAuditStatus, 1)
                            .eq(Music::getActivation, 0));
                    vo.setSongCount(songs.size());
                    vo.setTotalListen(songs.stream()
                            .map(Music::getListenNumb)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum());
                    return vo;
                })
                .sorted((a, b) -> Integer.compare(
                        b.getTotalListen() == null ? 0 : b.getTotalListen(),
                        a.getTotalListen() == null ? 0 : a.getTotalListen()))
                .limit(size)
                .collect(Collectors.toList());

        setCache(cacheKey, JSON.toJSONString(result));
        return result;
    }

    @Override
    public ArtistDetailVO artistDetail(Integer artistId) {
        if (artistId == null) {
            return null;
        }
        String cacheKey = "artist:detail:" + artistId;

        ArtistDetailVO cached = getCachedObject(cacheKey, ArtistDetailVO.class);
        if (cached != null) {
            return cached;
        }

        User singer = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, artistId)
                .eq(User::getRole, 1));
        if (singer == null) {
            return null;
        }

        List<Music> songs = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, artistId)
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getListenNumb));

        ArtistDetailVO vo = new ArtistDetailVO();
        vo.setId(singer.getId());
        vo.setUsername(singer.getUsername());
        vo.setImageUrl(singer.getImageUrl());
        vo.setAbout(singer.getAbout());
        vo.setSongCount(songs.size());
        vo.setTotalListen(songs.stream()
                .map(Music::getListenNumb)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        vo.setSongs(songs.stream().map(this::toMusicVO).collect(Collectors.toList()));

        setCache(cacheKey, JSON.toJSONString(vo));
        return vo;
    }

    /**
     * 安全获取缓存列表，Redis 不可用时返回 null
     */
    private <T> List<T> getCachedList(String cacheKey, Class<T> clazz) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return JSON.parseArray(cached, clazz);
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级为直接查询数据库: key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 安全获取缓存对象，Redis 不可用时返回 null
     */
    private <T> T getCachedObject(String cacheKey, Class<T> clazz) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return JSON.parseObject(cached, clazz);
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级为直接查询数据库: key={}, error={}", cacheKey, e.getMessage());
        }
        return null;
    }

    /**
     * 安全写入缓存，Redis 不可用时静默失败
     */
    private void setCache(String cacheKey, String value) {
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis 写入失败，跳过缓存: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 基于用户收藏歌曲的标签进行简单推荐；无收藏时返回热门歌曲。
     */
    private List<Music> recommendByUserLikes(Integer userId, int size) {
        List<Mylike> likes = mylikeMapper.selectList(new LambdaQueryWrapper<Mylike>()
                .eq(Mylike::getUserId, userId));
        if (likes.isEmpty()) {
            return topSongs(size);
        }

        List<Integer> likedMusicIds = likes.stream()
                .map(Mylike::getMusicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (likedMusicIds.isEmpty()) {
            return topSongs(size);
        }

        List<Music> likedSongs = musicMapper.selectBatchIds(likedMusicIds);
        Set<String> tags = new LinkedHashSet<>();
        for (Music song : likedSongs) {
            if (StringUtils.hasText(song.getTags())) {
                for (String tag : song.getTags().split(",")) {
                    String t = tag.trim();
                    if (StringUtils.hasText(t)) {
                        tags.add(t);
                    }
                }
            }
        }

        if (tags.isEmpty()) {
            return musicMapper.selectList(new LambdaQueryWrapper<Music>()
                    .eq(Music::getAuditStatus, 1)
                    .eq(Music::getActivation, 0)
                    .notIn(!likedMusicIds.isEmpty(), Music::getMusicId, likedMusicIds)
                    .orderByDesc(Music::getListenNumb)
                    .last("LIMIT " + size));
        }

        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<Music>()
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .notIn(!likedMusicIds.isEmpty(), Music::getMusicId, likedMusicIds);
        wrapper.and(w -> {
            boolean first = true;
            for (String tag : tags) {
                if (first) {
                    w.like(Music::getTags, tag);
                    first = false;
                } else {
                    w.or().like(Music::getTags, tag);
                }
            }
        });
        wrapper.orderByDesc(Music::getListenNumb).last("LIMIT " + size);
        return musicMapper.selectList(wrapper);
    }

    private List<Music> topSongs(int size) {
        return musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getListenNumb)
                .last("LIMIT " + size));
    }

    private MusicVO toMusicVO(Music music) {
        MusicVO vo = new MusicVO();
        vo.setMusicId(music.getMusicId());
        vo.setFromSinger(music.getFromSinger());
        vo.setMusicName(music.getMusicName());
        vo.setMusicUrl(music.getMusicUrl());
        vo.setActivation(music.getActivation());
        vo.setListenNumb(music.getListenNumb());
        vo.setImageUrl(music.getImageUrl());
        vo.setTimelength(music.getTimelength());
        vo.setCreateTime(music.getCreateTime());
        vo.setTags(music.getTags());
        vo.setLyric(music.getLyric());

        User singer = music.getFromSinger() == null ? null : userMapper.selectById(music.getFromSinger());
        if (singer != null) {
            vo.setSingerName(singer.getUsername());
        }
        return vo;
    }

    @Override
    public void recordPlay(Integer musicId) {
        if (musicId == null) {
            return;
        }
        // 原子更新播放量 +1
        int rows = musicMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Music>()
                        .eq(Music::getMusicId, musicId)
                        .setSql("listen_numb = listen_numb + 1"));
        if (rows > 0) {
            log.info("歌曲播放量 +1: musicId={}", musicId);
            // 清除相关缓存
            evictRecommendCache();
        }
    }

    @Override
    public void evictRecommendCache() {
        String[] patterns = {"recommend:*", "music:*", "artist:*"};
        try {
            Set<String> allKeys = new LinkedHashSet<>();
            for (String pattern : patterns) {
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    allKeys.addAll(keys);
                }
            }
            if (!allKeys.isEmpty()) {
                stringRedisTemplate.delete(allKeys);
                log.info("清理推荐模块 Redis 缓存成功, 共 {} 个 key", allKeys.size());
            }
        } catch (Exception e) {
            log.warn("清理推荐模块 Redis 缓存失败, 待 TTL 自动过期: error={}", e.getMessage());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private String safe(Integer value) {
        return value == null ? "default" : String.valueOf(value);
    }
}