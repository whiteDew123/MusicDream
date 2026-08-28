package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.MusicMapper;
import com.itheima.admin.service.MusicService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Music;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.util.StringUtils;

@Service
public class MusicServiceImpl implements MusicService {

    @Autowired
    private MusicMapper musicMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger log = LoggerFactory.getLogger(MusicServiceImpl.class);

    @Value("${recognize.service-url:http://localhost:8011}")
    private String recognizeServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 清理推荐模块 Redis 缓存；未配置 Redis 或清理失败时跳过
     */
    private void evictRecommendCache() {
        if (stringRedisTemplate == null) {
            return;
        }
        List<String> patterns = Arrays.asList("recommend:*", "music:*", "artist:*");
        try {
            Set<String> allKeys = new HashSet<>();
            for (String pattern : patterns) {
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    allKeys.addAll(keys);
                }
            }
            if (!allKeys.isEmpty()) {
                stringRedisTemplate.delete(allKeys);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public Result searchMusic(Integer pn, Integer size, String keyword) {
        Page<Music> musicPage = new Page<>(pn, size);
        QueryWrapper<Music> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(e -> e
                    .like("music_name", keyword)
                    .or()
                    .like("music_id", keyword)
                    .or()
                    .like("tags", keyword)
            );
        }
        Page<Music> page = musicMapper.selectPage(musicPage, wrapper);
        return Result.success("查询成功", page);
    }

    @Override
    public Result freezeMusic(Integer id) {
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        musicWrapper.eq("music_id", id).setSql("activation = 2");
        musicMapper.update(null, musicWrapper);
        return Result.success("冻结成功", null);
    }

    @Override
    public Result unFreezeMusic(Integer id) {
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        musicWrapper.eq("music_id", id).setSql("activation = 0");
        musicMapper.update(null, musicWrapper);
        return Result.success("解冻成功", null);
    }

    @Override
    public Result pagePendingMusic(Integer pn, Integer size) {
        Page<Music> musicPage = new Page<>(pn, size);
        QueryWrapper<Music> wrapper = new QueryWrapper<>();
        wrapper.eq("audit_status", 0);
        Page<Music> page = musicMapper.selectPage(musicPage, wrapper);
        return Result.success("查询成功", page);
    }

    @Override
    public Result approveMusic(Integer id) {
        Music music = musicMapper.selectById(id);
        if (music == null) {
            return Result.error(404, "歌曲不存在");
        }
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        musicWrapper.eq("music_id", id).setSql("audit_status = 1");
        musicMapper.update(null, musicWrapper);

        // 审核通过后异步注册听歌识曲指纹（不阻塞审核响应）
        if (StringUtils.hasText(music.getMusicUrl())) {
            final Integer musicId = id;
            CompletableFuture.runAsync(() -> {
                try {
                    URI uri = UriComponentsBuilder
                            .fromHttpUrl(recognizeServiceUrl + "/recognize/registerByUrl")
                            .queryParam("musicId", musicId)
                            .queryParam("musicUrl", music.getMusicUrl())
                            .build()
                            .toUri();
                    restTemplate.postForEntity(uri, null, String.class);
                    log.info("歌曲审核通过，指纹注册任务已触发: musicId={}", musicId);
                } catch (Exception e) {
                    log.error("歌曲指纹注册失败: musicId={}", musicId, e);
                }
            });
        }
        evictRecommendCache();
        return Result.success("审核通过", null);
    }

    @Override
    public Result rejectMusic(Integer id, String remark) {
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        musicWrapper.eq("music_id", id)
                .setSql("audit_status = 2");
        if (remark != null) {
            musicWrapper.set(true, "audit_remark", remark);
        }
        musicMapper.update(null, musicWrapper);
        evictRecommendCache();
        return Result.success("驳回成功", null);
    }

    @Override
    public Result deleteMusic(Integer id) {
        musicMapper.deleteById(id);
        evictRecommendCache();
        return Result.success("删除成功", null);
    }
}