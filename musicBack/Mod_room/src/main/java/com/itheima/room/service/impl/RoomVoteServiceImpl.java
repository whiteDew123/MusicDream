package com.itheima.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.room.entity.Room;
import com.itheima.room.entity.RoomMember;
import com.itheima.room.entity.RoomPlaylist;
import com.itheima.room.entity.RoomPlaylistVote;
import com.itheima.room.mapper.RoomMapper;
import com.itheima.room.mapper.RoomMemberMapper;
import com.itheima.room.mapper.RoomPlaylistMapper;
import com.itheima.room.mapper.RoomPlaylistVoteMapper;
import com.itheima.room.service.RoomVoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 切歌投票 Service 实现
 * <p>
 * 规则（已调整）：发起者除外 + 附议人数达到房间总人数的 50% 以上（向上取整）+ 30 秒超时。
 */
@Service
public class RoomVoteServiceImpl extends ServiceImpl<RoomPlaylistVoteMapper, RoomPlaylistVote>
        implements RoomVoteService {

    /** 附议有效期（秒） */
    private static final int VOTE_WINDOW_SECONDS = 30;

    private final RoomMapper roomMapper;
    private final RoomMemberMapper roomMemberMapper;
    private final RoomPlaylistMapper playlistMapper;

    public RoomVoteServiceImpl(RoomMapper roomMapper,
                               RoomMemberMapper roomMemberMapper,
                               RoomPlaylistMapper playlistMapper) {
        this.roomMapper = roomMapper;
        this.roomMemberMapper = roomMemberMapper;
        this.playlistMapper = playlistMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean skipVote(Long roomId, Long musicId, Long userId) {
        Room room = getActiveRoom(roomId);
        // 校验目标歌曲确在房间歌单中
        RoomPlaylist p = playlistMapper.selectOne(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, roomId)
                .eq(RoomPlaylist::getMusicId, musicId));
        if (p == null) {
            throw new IllegalArgumentException("该歌曲不在房间歌单中");
        }
        // 发起者不可重复为同一首歌投票
        RoomPlaylistVote exist = getOne(new LambdaQueryWrapper<RoomPlaylistVote>()
                .eq(RoomPlaylistVote::getRoomId, roomId)
                .eq(RoomPlaylistVote::getMusicId, musicId)
                .eq(RoomPlaylistVote::getUserId, userId));
        if (exist != null) {
            throw new IllegalArgumentException("你已经投过票了");
        }
        RoomPlaylistVote vote = new RoomPlaylistVote();
        vote.setRoomId(roomId);
        vote.setMusicId(musicId);
        vote.setUserId(userId);
        vote.setCreatedAt(LocalDateTime.now());
        save(vote);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean agreeVote(Long roomId, Long musicId, Long userId) {
        Room room = getActiveRoom(roomId);
        Long votedMusicId = musicId != null ? musicId : room.getCurrentMusicId();
        if (votedMusicId == null) {
            throw new IllegalArgumentException("当前没有可表决的歌曲");
        }
        if (userId.equals(room.getOwnerId())) {
            throw new IllegalArgumentException("房主不能参与切歌投票");
        }

        // 自己是否已投
        RoomPlaylistVote mine = getOne(new LambdaQueryWrapper<RoomPlaylistVote>()
                .eq(RoomPlaylistVote::getRoomId, roomId)
                .eq(RoomPlaylistVote::getMusicId, votedMusicId)
                .eq(RoomPlaylistVote::getUserId, userId));
        if (mine != null) {
            throw new IllegalArgumentException("你已经投过票了");
        }

        // 取该歌单所有投票，按时间升序（首个为发起者）
        List<RoomPlaylistVote> votes = list(new LambdaQueryWrapper<RoomPlaylistVote>()
                .eq(RoomPlaylistVote::getRoomId, roomId)
                .eq(RoomPlaylistVote::getMusicId, votedMusicId)
                .orderByAsc(RoomPlaylistVote::getCreatedAt));
        if (votes.isEmpty()) {
            throw new IllegalArgumentException("当前没有待表决的切歌投票");
        }
        // 30 秒超时判断：以最早投票时间为准
        LocalDateTime earliest = votes.get(0).getCreatedAt();
        if (earliest == null || earliest.isBefore(LocalDateTime.now().minusSeconds(VOTE_WINDOW_SECONDS))) {
            throw new IllegalArgumentException("投票已过期");
        }

        // 记录当前用户附议
        RoomPlaylistVote vote = new RoomPlaylistVote();
        vote.setRoomId(roomId);
        vote.setMusicId(votedMusicId);
        vote.setUserId(userId);
        vote.setCreatedAt(LocalDateTime.now());
        save(vote);

        // 附议人数需达到房间总人数的 50% 以上（向上取整；发起者不计入附议数）
        long totalMembers = roomMemberMapper.selectCount(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId));
        int required = (int) Math.ceil(totalMembers * 0.5);
        // 首个投票人为发起者（不参与附议计数）
        Long initiator = votes.get(0).getUserId();
        Set<Long> voters = votes.stream().map(RoomPlaylistVote::getUserId).collect(Collectors.toSet());
        voters.add(userId);
        long agreeCount = voters.stream().filter(v -> !v.equals(initiator)).count();
        if (agreeCount >= required) {
            advanceMusic(room);
            // 清除本首歌的投票记录，进入下一轮
            remove(new LambdaQueryWrapper<RoomPlaylistVote>()
                    .eq(RoomPlaylistVote::getRoomId, roomId)
                    .eq(RoomPlaylistVote::getMusicId, votedMusicId));
            return true;
        }
        return false;
    }

    /** 校验房间存在且未关闭 */
    private Room getActiveRoom(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (Objects.equals(room.getStatus(), 2)) {
            throw new IllegalArgumentException("房间已关闭");
        }
        return room;
    }

    /** 推进到下一首（依据播放模式决定循环或停止） */
    private void advanceMusic(Room room) {
        List<RoomPlaylist> playlist = playlistMapper.selectList(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, room.getId())
                .orderByAsc(RoomPlaylist::getSortOrder)
                .orderByAsc(RoomPlaylist::getId));
        if (playlist.isEmpty()) {
            room.setCurrentMusicId(null);
            room.setCurrentProgress(0.0);
            room.setIsPlaying(0);
            room.setStatus(0);
            roomMapper.updateById(room);
            return;
        }
        int idx = -1;
        for (int i = 0; i < playlist.size(); i++) {
            if (Objects.equals(playlist.get(i).getMusicId(), room.getCurrentMusicId())) {
                idx = i;
                break;
            }
        }
        RoomPlaylist next;
        if (idx == -1) {
            next = playlist.get(0);
        } else if (idx == playlist.size() - 1) {
            if (room.getPlayMode() != null && room.getPlayMode() == 1) {
                // 播放完毕停止
                room.setCurrentMusicId(null);
                room.setCurrentProgress(0.0);
                room.setIsPlaying(0);
                room.setStatus(0);
                roomMapper.updateById(room);
                return;
            }
            next = playlist.get(0); // 循环播放
        } else {
            next = playlist.get(idx + 1);
        }
        room.setCurrentMusicId(next.getMusicId());
        room.setCurrentProgress(0.0);
        room.setIsPlaying(1);
        room.setStatus(1);
        roomMapper.updateById(room);
    }
}
