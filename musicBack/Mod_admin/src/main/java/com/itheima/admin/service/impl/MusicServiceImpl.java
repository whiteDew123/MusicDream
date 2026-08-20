package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.MusicMapper;
import com.itheima.admin.service.MusicService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Music;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MusicServiceImpl implements MusicService {

    @Autowired
    private MusicMapper musicMapper;

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
        wrapper.orderByDesc("create_time");
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
}