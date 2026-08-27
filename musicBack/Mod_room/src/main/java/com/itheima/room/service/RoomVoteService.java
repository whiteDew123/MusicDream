package com.itheima.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.room.entity.RoomPlaylistVote;

/**
 * 切歌投票 Service 接口
 */
public interface RoomVoteService extends IService<RoomPlaylistVote> {

    /**
     * 发起切歌投票（发起者不参与附议计数）
     *
     * @return true 表示已发起投票
     */
    boolean skipVote(Long roomId, Long musicId, Long userId);

    /**
     * 附议切歌投票；附议人数达到房间总人数的 50% 以上（向上取整，发起者除外）且在 30 秒内则切歌成功。
     *
     * @return true 表示切歌成功（已跳至下一首），false 表示仍需等待更多附议
     */
    boolean agreeVote(Long roomId, Long musicId, Long userId);
}
