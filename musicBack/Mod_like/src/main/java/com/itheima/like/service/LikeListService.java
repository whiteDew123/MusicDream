package com.itheima.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.like.entity.LikeList;

import java.util.List;

/**
 * 收藏歌单 Service 接口
 */
public interface LikeListService extends IService<LikeList> {

    /**
     * 查询用户收藏的歌单列表（联表返回歌单详情）
     */
    List<LikeList> getLikedList(Integer userId);

    /**
     * 检查是否已收藏
     */
    boolean isLiked(Integer userId, Integer listId);

    /**
     * 新增收藏
     */
    void addLike(Integer userId, Integer listId);

    /**
     * 移除收藏
     */
    void removeLike(Integer userId, Integer listId);
}
