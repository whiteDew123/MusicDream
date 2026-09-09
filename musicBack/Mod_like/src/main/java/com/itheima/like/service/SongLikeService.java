package com.itheima.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.like.entity.SongLike;

import java.util.Map;

/**
 * 歌曲点赞 Service 接口
 */
public interface SongLikeService extends IService<SongLike> {

    /**
     * 切换点赞状态（已赞→取消，未赞→点赞）
     *
     * @param musicId 歌曲ID
     * @param userId  用户ID
     * @return {"liked": true/false, "likesCount": 当前点赞数}
     */
    Map<String, Object> toggleLike(Integer musicId, Integer userId);

    /**
     * 查询点赞状态 + 点赞数
     *
     * @param musicId 歌曲ID
     * @param userId  用户ID（未登录时传 null，只返回点赞数）
     * @return {"liked": true/false, "likesCount": 当前点赞数}
     */
    Map<String, Object> getLikeStatus(Integer musicId, Integer userId);

    /**
     * 统一查询歌曲交互统计（点赞状态 + 点赞数 + 评论数 + 分享数）
     * <p>
     * 一次请求返回全部数据，前端只需 1 次 HTTP 调用。
     *
     * @param musicId 歌曲ID
     * @param userId  用户ID（未登录时传 null，liked 恒为 false）
     * @return {"liked": true/false, "likesCount": N, "commentCount": N, "shareCount": N}
     */
    Map<String, Object> getMusicStats(Integer musicId, Integer userId);
}
