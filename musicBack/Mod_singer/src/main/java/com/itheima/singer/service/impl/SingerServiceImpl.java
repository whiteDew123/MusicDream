package com.itheima.singer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.domain.common.PageResult;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import com.itheima.singer.dto.MusicDTO;
import com.itheima.singer.mapper.MusicMapper;
import com.itheima.singer.mapper.UserMapper;
import com.itheima.singer.service.SingerService;
import com.itheima.singer.util.ReviewResult;
import com.itheima.singer.util.SensitiveWordUtil;
import com.itheima.singer.vo.MusicVO;
import com.itheima.singer.vo.SingerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 歌手模块业务实现
 *
 * <p>使用 MyBatis Plus 条件构造器 + 分页插件完成歌曲管理。</p>
 */
@Service
@RequiredArgsConstructor
public class SingerServiceImpl implements SingerService {

    private final MusicMapper musicMapper;
    private final UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(SingerServiceImpl.class);

    @Value("${recognize.service-url:http://localhost:8011}")
    private String recognizeServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> getDashboard(Integer singerId) {
        Map<String, Object> data = new LinkedHashMap<>();

        if (singerId == null) {
            data.put("myMusicTotal", 0);
            data.put("totalListenNumb", 0);
            data.put("pendingAuditTotal", 0);
            data.put("approvedTotal", 0);
            data.put("myTopMusic", List.of());
            data.put("recentUploads", List.of());
            return data;
        }

        // 我的歌曲总数
        Long myMusicTotal = musicMapper.selectCount(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId));
        // 总播放量（仅已通过审核且未冻结的歌曲）
        List<Music> activeMusic = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0));
        long totalListenNumb = activeMusic.stream().mapToLong(m -> m.getListenNumb() == null ? 0 : m.getListenNumb()).sum();
        // 待审核数
        Long pendingAuditTotal = musicMapper.selectCount(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .eq(Music::getAuditStatus, 0));
        // 已通过数
        Long approvedTotal = musicMapper.selectCount(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .eq(Music::getAuditStatus, 1));

        // 我的歌曲播放排行 TOP5
        List<Music> topList = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0)
                .orderByDesc(Music::getListenNumb)
                .last("LIMIT 5"));
        List<MusicVO> myTopMusic = topList.stream().map(this::toMusicVO).collect(Collectors.toList());

        // 最近上传的 5 首
        List<Music> recentList = musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .orderByDesc(Music::getCreateTime)
                .last("LIMIT 5"));
        List<MusicVO> recentUploads = recentList.stream().map(this::toMusicVO).collect(Collectors.toList());

        data.put("myMusicTotal", myMusicTotal == null ? 0 : myMusicTotal.intValue());
        data.put("totalListenNumb", totalListenNumb);
        data.put("pendingAuditTotal", pendingAuditTotal == null ? 0 : pendingAuditTotal.intValue());
        data.put("approvedTotal", approvedTotal == null ? 0 : approvedTotal.intValue());
        data.put("myTopMusic", myTopMusic);
        data.put("recentUploads", recentUploads);

        return data;
    }

    @Override
    public PageResult<MusicVO> pageSongs(Integer singerId, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : Math.min(size, 100);

        Page<Music> p = new Page<>(current, pageSize);

        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<Music>()
                .orderByDesc(Music::getCreateTime)
                .orderByDesc(Music::getListenNumb);
        if (singerId != null) {
            wrapper.eq(Music::getFromSinger, singerId);
        } else {
            wrapper.eq(Music::getAuditStatus, 1)
                   .eq(Music::getActivation, 0);
        }

        musicMapper.selectPage(p, wrapper);

        List<MusicVO> records = p.getRecords().stream()
                .map(this::toMusicVO)
                .collect(Collectors.toList());

        return new PageResult<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getPages(), records);
    }

    @Override
    public MusicVO publishSong(MusicDTO dto) {
        // 自动审核
        ReviewResult review = SensitiveWordUtil.autoReview(dto);

        Music music = new Music();
        music.setFromSinger(dto.getFromSinger());
        music.setMusicName(dto.getMusicName());
        music.setMusicUrl(dto.getMusicUrl());
        music.setImageUrl(dto.getImageUrl());
        music.setTimelength(dto.getTimelength());
        music.setTags(dto.getTags());
        music.setLyric(dto.getLyric());
        music.setActivation(0);
        // 根据自动审核结果设置审核状态
        if (review.isPass()) {
            music.setAuditStatus(1);          // 直接公开
        } else if (review.isReject()) {
            music.setAuditStatus(2);          // 自动驳回
            music.setAuditRemark(review.getMessage());
        } else {
            music.setAuditStatus(0);          // 进入管理员审核列表
        }
        music.setListenNumb(0);
        music.setCreateTime(LocalDate.now());

        musicMapper.insert(music);

        // 自动审核通过后，同步触发听歌识曲指纹注册（失败不影响发布）
        if (review.isPass() && StringUtils.hasText(music.getMusicUrl())) {
            triggerFingerprintRegister(music);
        }
        return toMusicVO(music);
    }

    @Override
    public MusicVO updateSong(Integer musicId, MusicDTO dto) {
        Music exist = musicMapper.selectById(musicId);
        if (exist == null) {
            return null;
        }

        LambdaUpdateWrapper<Music> wrapper = new LambdaUpdateWrapper<Music>()
                .eq(Music::getMusicId, musicId);

        if (dto.getFromSinger() != null) {
            wrapper.set(Music::getFromSinger, dto.getFromSinger());
        }
        if (StringUtils.hasText(dto.getMusicName())) {
            wrapper.set(Music::getMusicName, dto.getMusicName());
        }
        if (StringUtils.hasText(dto.getMusicUrl())) {
            wrapper.set(Music::getMusicUrl, dto.getMusicUrl());
        }
        if (StringUtils.hasText(dto.getImageUrl())) {
            wrapper.set(Music::getImageUrl, dto.getImageUrl());
        }
        if (dto.getTimelength() != null) {
            wrapper.set(Music::getTimelength, dto.getTimelength());
        }
        if (StringUtils.hasText(dto.getTags())) {
            wrapper.set(Music::getTags, dto.getTags());
        }
        if (StringUtils.hasText(dto.getLyric())) {
            wrapper.set(Music::getLyric, dto.getLyric());
        }

        musicMapper.update(null, wrapper);
        return toMusicVO(musicMapper.selectById(musicId));
    }

    @Override
    public boolean deleteSong(Integer musicId) {
        return musicMapper.deleteById(musicId) > 0;
    }

    @Override
    public boolean freezeSong(Integer musicId) {
        LambdaUpdateWrapper<Music> wrapper = new LambdaUpdateWrapper<Music>()
                .eq(Music::getMusicId, musicId)
                .set(Music::getActivation, 2);
        return musicMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean unfreezeSong(Integer musicId) {
        LambdaUpdateWrapper<Music> wrapper = new LambdaUpdateWrapper<Music>()
                .eq(Music::getMusicId, musicId)
                .set(Music::getActivation, 0);
        return musicMapper.update(null, wrapper) > 0;
    }

    @Override
    public SingerVO getSingerInfo(Integer singerId) {
        if (singerId == null) {
            return null;
        }

        User singer = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, singerId)
                .eq(User::getRole, 1));
        if (singer == null) {
            return null;
        }

        Long songCount = musicMapper.selectCount(new LambdaQueryWrapper<Music>()
                .eq(Music::getFromSinger, singerId)
                .eq(Music::getAuditStatus, 1)
                .eq(Music::getActivation, 0));

        SingerVO vo = new SingerVO();
        vo.setId(singer.getId());
        vo.setUsername(singer.getUsername());
        vo.setEmail(singer.getEmail());
        vo.setPhone(singer.getPhone());
        vo.setImageUrl(singer.getImageUrl());
        vo.setAbout(singer.getAbout());
        vo.setCreateTime(singer.getCreateTime());
        vo.setSongCount(songCount == null ? 0 : songCount.intValue());
        return vo;
    }

    private MusicVO toMusicVO(Music music) {
        MusicVO vo = new MusicVO();
        vo.setMusicId(music.getMusicId());
        vo.setFromSinger(music.getFromSinger());
        vo.setMusicName(music.getMusicName());
        vo.setMusicUrl(music.getMusicUrl());
        vo.setActivation(music.getActivation());
        vo.setAuditStatus(music.getAuditStatus());
        vo.setAuditRemark(music.getAuditRemark());
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

    /**
     * 调用听歌识曲服务注册指纹
     */
    private void triggerFingerprintRegister(Music music) {
        try {
            String encodedUrl = URLEncoder.encode(music.getMusicUrl(), StandardCharsets.UTF_8);
            String url = recognizeServiceUrl + "/recognize/registerByUrl?musicId=" + music.getMusicId()
                    + "&musicUrl=" + encodedUrl;
            restTemplate.postForEntity(url, null, String.class);
            log.info("歌曲发布自动通过，指纹注册任务已触发: musicId={}", music.getMusicId());
        } catch (Exception e) {
            log.error("歌曲指纹注册失败: musicId={}", music.getMusicId(), e);
        }
    }
}