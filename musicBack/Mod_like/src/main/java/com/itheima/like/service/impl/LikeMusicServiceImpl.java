package com.itheima.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.like.entity.LikeMusic;
import com.itheima.like.mapper.LikeMusicMapper;
import com.itheima.like.service.LikeMusicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeMusicServiceImpl extends ServiceImpl<LikeMusicMapper, LikeMusic> implements LikeMusicService {

    @Override
    public List<LikeMusic> getLikedMusic(Integer userId) {
        return baseMapper.selectLikedMusicByUserId(userId);
    }
}
