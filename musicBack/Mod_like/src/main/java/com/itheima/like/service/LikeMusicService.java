package com.itheima.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.like.entity.LikeMusic;

import java.util.List;

/**
 * 收藏歌曲 Service 接口
 */
public interface LikeMusicService extends IService<LikeMusic> {

    /**
     * 查询用户收藏的歌曲列表（联表返回歌曲详情）
     */
    List<LikeMusic> getLikedMusic(Integer userId);

    /**
     * 检查是否已收藏
     */
    boolean isLiked(Integer userId, Integer musicId);

    /**
     * 新增收藏
     */
    void addLike(Integer userId, Integer musicId);

    /**
     * 移除收藏
     */
    void removeLike(Integer userId, Integer musicId);
}
