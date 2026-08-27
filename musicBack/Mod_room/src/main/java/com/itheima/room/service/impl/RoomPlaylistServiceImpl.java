package com.itheima.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import com.itheima.room.dto.PlaylistSortDTO;
import com.itheima.room.entity.Room;
import com.itheima.room.entity.RoomPlaylist;
import com.itheima.room.mapper.MusicMapper;
import com.itheima.room.mapper.RoomMapper;
import com.itheima.room.mapper.RoomPlaylistMapper;
import com.itheima.room.mapper.UserMapper;
import com.itheima.room.service.RoomPlaylistService;
import com.itheima.room.vo.RoomPlaylistItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 房间歌单 Service 实现
 */
@Service
public class RoomPlaylistServiceImpl extends ServiceImpl<RoomPlaylistMapper, RoomPlaylist>
        implements RoomPlaylistService {

    private final RoomMapper roomMapper;
    private final MusicMapper musicMapper;
    private final UserMapper userMapper;

    public RoomPlaylistServiceImpl(RoomMapper roomMapper,
                                   MusicMapper musicMapper,
                                   UserMapper userMapper) {
        this.roomMapper = roomMapper;
        this.musicMapper = musicMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<RoomPlaylistItemVO> getList(Long roomId, Long userId) {
        List<RoomPlaylist> items = list(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, roomId)
                .orderByAsc(RoomPlaylist::getSortOrder)
                .orderByAsc(RoomPlaylist::getId));

        List<Long> musicIds = items.stream().map(RoomPlaylist::getMusicId).distinct().collect(Collectors.toList());
        Map<Integer, Music> musicMap = loadMusicMap(musicIds);
        List<Long> addedByIds = items.stream()
                .map(RoomPlaylist::getAddedBy)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, User> userMap = loadUserMap(addedByIds);

        List<RoomPlaylistItemVO> result = new ArrayList<>();
        for (RoomPlaylist p : items) {
            RoomPlaylistItemVO vo = new RoomPlaylistItemVO();
            vo.setPlaylistId(p.getId());
            vo.setMusicId(p.getMusicId());
            vo.setSortOrder(p.getSortOrder());
            vo.setStatus(p.getStatus());
            vo.setAddedBy(p.getAddedBy());
            Music music = musicMap.get(p.getMusicId().intValue());
            if (music != null) {
                vo.setMusicName(music.getMusicName());
                vo.setCover(music.getImageUrl());
                vo.setDuration(music.getTimelength());
                vo.setMusicUrl(music.getMusicUrl());
            }
            if (p.getAddedBy() != null) {
                User u = userMap.get(p.getAddedBy().intValue());
                vo.setAddedByName(u == null ? null : u.getUsername());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long roomId, Long musicId, Long userId) {
        Music music = musicMapper.selectById(musicId.intValue());
        if (music == null) {
            throw new IllegalArgumentException("歌曲不存在");
        }
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (Objects.equals(room.getStatus(), 2)) {
            throw new IllegalArgumentException("房间已关闭");
        }
        // 重复添加校验：room_id + music_id
        RoomPlaylist existing = getOne(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, roomId)
                .eq(RoomPlaylist::getMusicId, musicId));
        if (existing != null) {
            throw new IllegalArgumentException("这首歌已在房间歌单中");
        }

        RoomPlaylist p = new RoomPlaylist();
        p.setRoomId(roomId);
        p.setMusicId(musicId);
        p.setAddedBy(userId);
        p.setSortOrder(nextSortOrder(roomId));
        p.setStatus(0);
        p.setAddTime(LocalDateTime.now());
        save(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long roomId, Long musicId, Long userId) {
        RoomPlaylist existing = getOne(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, roomId)
                .eq(RoomPlaylist::getMusicId, musicId));
        if (existing == null) {
            throw new IllegalArgumentException("歌曲不在房间歌单中");
        }
        removeById(existing.getId());

        // 若移除的是当前播放歌曲，重置当前进度到下一首或置空（避免残留失效歌曲）
        Room room = roomMapper.selectById(roomId);
        if (room != null && Objects.equals(room.getCurrentMusicId(), musicId)) {
            RoomPlaylist next = getOne(new LambdaQueryWrapper<RoomPlaylist>()
                    .eq(RoomPlaylist::getRoomId, roomId)
                    .orderByAsc(RoomPlaylist::getSortOrder)
                    .orderByAsc(RoomPlaylist::getId)
                    .last("LIMIT 1"));
            if (next == null) {
                room.setCurrentMusicId(null);
                room.setCurrentProgress(0.0);
                room.setIsPlaying(0);
                room.setStatus(0);
            } else {
                room.setCurrentMusicId(next.getMusicId());
                room.setCurrentProgress(0.0);
                room.setIsPlaying(1);
                room.setStatus(1);
            }
            roomMapper.updateById(room);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(Long roomId, PlaylistSortDTO dto, Long userId) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        for (PlaylistSortDTO.Item item : dto.getItems()) {
            RoomPlaylist p = getOne(new LambdaQueryWrapper<RoomPlaylist>()
                    .eq(RoomPlaylist::getRoomId, roomId)
                    .eq(RoomPlaylist::getMusicId, item.getMusicId()));
            if (p != null) {
                p.setSortOrder(item.getSortOrder());
                updateById(p);
            }
        }
    }

    // ======================== 私有辅助方法 ========================

    /** 计算当前房间歌单的最大 sort_order + 1 */
    private int nextSortOrder(Long roomId) {
        RoomPlaylist last = getOne(new LambdaQueryWrapper<RoomPlaylist>()
                .eq(RoomPlaylist::getRoomId, roomId)
                .orderByDesc(RoomPlaylist::getSortOrder)
                .orderByDesc(RoomPlaylist::getId)
                .last("LIMIT 1"));
        return (last == null || last.getSortOrder() == null) ? 0 : last.getSortOrder() + 1;
    }

    private Map<Integer, Music> loadMusicMap(List<Long> musicIds) {
        if (musicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return musicMapper.selectBatchIds(musicIds.stream().map(Long::intValue).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Music::getMusicId, m -> m));
    }

    private Map<Integer, User> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds.stream().map(Long::intValue).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }
}
