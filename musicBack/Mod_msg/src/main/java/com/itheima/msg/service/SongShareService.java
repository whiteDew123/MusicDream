package com.itheima.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.msg.entity.SongShare;

import java.util.Map;

/**
 * 歌曲分享 Service 接口
 */
public interface SongShareService extends IService<SongShare> {

    /**
     * 记录分享事件 + 生成分享链接
     * <p>
     * 去重策略：同一用户同一渠道每天只记 1 次（避免刷量），但 share_count 仍 +1
     *
     * @param musicId 歌曲ID
     * @param userId  分享用户ID
     * @param channel 渠道: link/qrcode/weibo/wechat/copy
     * @return {"shareUrl": 分享落地页URL, "shareCount": 当前总分享数}
     */
    Map<String, Object> recordShare(Integer musicId, Integer userId, String channel);

    /**
     * 查询歌曲分享数（从 music.share_count 冗余列读取）
     */
    int countByMusicId(Integer musicId);
}
