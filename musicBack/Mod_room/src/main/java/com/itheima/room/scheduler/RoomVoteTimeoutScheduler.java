package com.itheima.room.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.room.entity.RoomPlaylistVote;
import com.itheima.room.mapper.RoomPlaylistVoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 切歌投票超时清理任务
 * <p>
 * 每 5 秒扫描一次：distinct 校验「最早投票时间超过 30 秒」的投票，整组清除并广播 expired。
 * 这样投票在 30s 后自动失效，不再依赖某位成员再次附议才触发超时。
 */
@Component
public class RoomVoteTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(RoomVoteTimeoutScheduler.class);

    /** 投票有效期（秒） */
    private static final int VOTE_WINDOW_SECONDS = 30;

    private final RoomPlaylistVoteMapper voteMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomVoteTimeoutScheduler(RoomPlaylistVoteMapper voteMapper,
                                    SimpMessagingTemplate messagingTemplate) {
        this.voteMapper = voteMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 5000)
    public void cleanupExpired() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(VOTE_WINDOW_SECONDS);
        List<RoomPlaylistVote> expired = voteMapper.selectList(new LambdaQueryWrapper<RoomPlaylistVote>()
                .lt(RoomPlaylistVote::getCreatedAt, threshold));
        if (expired.isEmpty()) {
            return;
        }
        // 按 (roomId, musicId) 分组
        Map<String, List<RoomPlaylistVote>> byKey = expired.stream()
                .collect(Collectors.groupingBy(v -> v.getRoomId() + ":" + v.getMusicId()));
        for (Map.Entry<String, List<RoomPlaylistVote>> e : byKey.entrySet()) {
            String[] parts = e.getKey().split(":");
            Long roomId = Long.valueOf(parts[0]);
            Long musicId = Long.valueOf(parts[1]);
            // 仅删除过期的票，避免误杀同一首歌刚重新发起的新票
            List<Long> expiredIds = e.getValue().stream().map(RoomPlaylistVote::getId).collect(Collectors.toList());
            voteMapper.deleteBatchIds(expiredIds);
            // 仅当该歌已无任何有效票时，才广播超时失效
            long remaining = voteMapper.selectCount(new LambdaQueryWrapper<RoomPlaylistVote>()
                    .eq(RoomPlaylistVote::getRoomId, roomId)
                    .eq(RoomPlaylistVote::getMusicId, musicId));
            if (remaining == 0) {
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/skip-vote",
                        Map.of("musicId", musicId, "action", "expired"));
                log.info("房间 {} 歌曲 {} 切歌投票已超时清除", roomId, musicId);
            }
        }
    }
}
