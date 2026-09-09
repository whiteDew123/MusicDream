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

    @Override
    public boolean isLiked(Integer userId, Integer musicId) {
        return baseMapper.countByUserAndMusic(userId, musicId) > 0;
    }

    @Override
    public void addLike(Integer userId, Integer musicId) {
        baseMapper.insertLike(userId, musicId);
    }

    @Override
    public void removeLike(Integer userId, Integer musicId) {
        baseMapper.deleteLike(userId, musicId);
    }
}
