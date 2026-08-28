package com.itheima.musicbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.musicbox.dto.MusicBoxCreateDTO;
import com.itheima.musicbox.entity.*;
import com.itheima.musicbox.mapper.*;
import com.itheima.musicbox.service.MusicBoxService;
import com.itheima.musicbox.vo.MusicBoxFriendRequestVO;
import com.itheima.musicbox.vo.MusicBoxPlazaVO;
import com.itheima.musicbox.vo.MusicBoxSongVO;
import com.itheima.musicbox.vo.MusicBoxVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MusicBoxServiceImpl implements MusicBoxService {

    private final MusicBoxMapper musicBoxMapper;
    private final MusicBoxSongMapper musicBoxSongMapper;
    private final MusicBoxOpenRecordMapper openRecordMapper;
    private final MusicBoxLikeMapper likeMapper;
    private final MusicBoxFriendRequestMapper friendRequestMapper;

    public MusicBoxServiceImpl(MusicBoxMapper musicBoxMapper,
                               MusicBoxSongMapper musicBoxSongMapper,
                               MusicBoxOpenRecordMapper openRecordMapper,
                               MusicBoxLikeMapper likeMapper,
                               MusicBoxFriendRequestMapper friendRequestMapper) {
        this.musicBoxMapper = musicBoxMapper;
        this.musicBoxSongMapper = musicBoxSongMapper;
        this.openRecordMapper = openRecordMapper;
        this.likeMapper = likeMapper;
        this.friendRequestMapper = friendRequestMapper;
    }

    @Override
    @Transactional
    public Integer createBox(Integer userId, MusicBoxCreateDTO dto) {
        // 验证歌曲数量
        if (dto.getSongIds() == null || dto.getSongIds().size() < 3 || dto.getSongIds().size() > 5) {
            throw new RuntimeException("盲盒必须包含3-5首歌曲");
        }

        // 创建盲盒
        MusicBox box = new MusicBox();
        box.setUserId(userId);
        box.setTitle(dto.getTitle());
        box.setMoodTag(dto.getMoodTag());
        box.setMessage(dto.getMessage());
        box.setOpenCount(0);
        box.setLikeCount(0);
        box.setStatus(0);
        musicBoxMapper.insert(box);

        // 添加歌曲
        for (int i = 0; i < dto.getSongIds().size(); i++) {
            MusicBoxSong song = new MusicBoxSong();
            song.setBoxId(box.getId());
            song.setSongId(dto.getSongIds().get(i));
            song.setSortOrder(i + 1);
            musicBoxSongMapper.insert(song);
        }

        return box.getId();
    }

    @Override
    public List<MusicBoxPlazaVO> getPlazaList(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        return musicBoxMapper.selectPlazaList(userId, offset, size);
    }

    @Override
    public List<MusicBoxPlazaVO> getPlazaListByTag(Integer userId, String tag, int page, int size) {
        int offset = (page - 1) * size;
        return musicBoxMapper.selectPlazaListByTag(userId, tag, offset, size);
    }

    @Override
    public List<MusicBoxPlazaVO> getHotBoxes(Integer userId, int limit) {
        return musicBoxMapper.selectHotBoxes(userId, limit);
    }

    @Override
    public List<MusicBoxPlazaVO> getRandomBoxes(Integer userId, int limit) {
        return musicBoxMapper.selectRandomBoxes(userId, limit);
    }

    @Override
    public MusicBoxVO getBoxDetail(Integer boxId, Integer userId) {
        MusicBoxVO vo = musicBoxMapper.selectBoxDetail(boxId, userId);
        if (vo != null) {
            vo.setSongs(musicBoxMapper.selectBoxSongs(boxId));
        }
        return vo;
    }

    @Override
    @Transactional
    public MusicBoxVO openBox(Integer boxId, Integer userId) {
        // 检查是否已开启过
        LambdaQueryWrapper<MusicBoxOpenRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MusicBoxOpenRecord::getBoxId, boxId)
               .eq(MusicBoxOpenRecord::getUserId, userId);
        MusicBoxOpenRecord existing = openRecordMapper.selectOne(wrapper);

        if (existing == null) {
            // 记录开启
            MusicBoxOpenRecord record = new MusicBoxOpenRecord();
            record.setBoxId(boxId);
            record.setUserId(userId);
            openRecordMapper.insert(record);

            // 更新开启次数
            musicBoxMapper.update(null,
                new LambdaUpdateWrapper<MusicBox>()
                    .eq(MusicBox::getId, boxId)
                    .setSql("open_count = open_count + 1"));
        }

        // 查询详情
        MusicBoxVO vo = musicBoxMapper.selectBoxDetail(boxId, userId);
        if (vo != null) {
            vo.setSongs(musicBoxMapper.selectBoxSongs(boxId));
        }
        return vo;
    }

    @Override
    @Transactional
    public void toggleLike(Integer boxId, Integer userId) {
        // 检查是否已点赞
        LambdaQueryWrapper<MusicBoxLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MusicBoxLike::getBoxId, boxId)
               .eq(MusicBoxLike::getUserId, userId);
        MusicBoxLike existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            // 取消点赞
            likeMapper.delete(wrapper);
            musicBoxMapper.update(null,
                new LambdaUpdateWrapper<MusicBox>()
                    .eq(MusicBox::getId, boxId)
                    .setSql("like_count = like_count - 1"));
        } else {
            // 点赞
            MusicBoxLike like = new MusicBoxLike();
            like.setBoxId(boxId);
            like.setUserId(userId);
            likeMapper.insert(like);
            musicBoxMapper.update(null,
                new LambdaUpdateWrapper<MusicBox>()
                    .eq(MusicBox::getId, boxId)
                    .setSql("like_count = like_count + 1"));
        }
    }

    @Override
    public List<MusicBoxPlazaVO> getMyBoxes(Integer userId) {
        return musicBoxMapper.selectMyBoxes(userId);
    }

    @Override
    public List<MusicBoxPlazaVO> getOpenedBoxes(Integer userId) {
        return musicBoxMapper.selectOpenedBoxes(userId);
    }

    @Override
    public List<MusicBoxPlazaVO> getLikedBoxes(Integer userId) {
        return musicBoxMapper.selectLikedBoxes(userId);
    }

    @Override
    public void deleteBox(Integer boxId, Integer userId) {
        musicBoxMapper.update(null,
            new LambdaUpdateWrapper<MusicBox>()
                .eq(MusicBox::getId, boxId)
                .eq(MusicBox::getUserId, userId)
                .set(MusicBox::getStatus, 1));
    }

    @Override
    public void sendFriendRequest(Integer boxId, Integer senderId, Integer receiverId, String message) {
        // 检查是否已是好友（通过 Mod_friend 的表）
        // 这里简化处理，直接检查是否有待处理请求

        // 检查是否已有待处理请求
        int count = friendRequestMapper.countPendingRequest(senderId, receiverId);
        if (count > 0) {
            throw new RuntimeException("已有待处理的交友请求");
        }

        MusicBoxFriendRequest request = new MusicBoxFriendRequest();
        request.setBoxId(boxId);
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setMessage(message);
        request.setStatus(0);
        friendRequestMapper.insert(request);
    }

    @Override
    public List<MusicBoxFriendRequestVO> getReceivedFriendRequests(Integer receiverId) {
        return friendRequestMapper.selectReceivedWithInfo(receiverId, 0);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Integer requestId, Integer userId) {
        MusicBoxFriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getReceiverId().equals(userId)) {
            throw new RuntimeException("请求不存在或无权操作");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        // 更新请求状态
        request.setStatus(1);
        friendRequestMapper.updateById(request);

        // 注意：这里需要调用 Mod_friend 的服务来建立好友关系
        // 由于微服务隔离，这里只更新状态，实际好友关系由前端调用 Mod_friend 接口完成
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Integer requestId, Integer userId) {
        MusicBoxFriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getReceiverId().equals(userId)) {
            throw new RuntimeException("请求不存在或无权操作");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        request.setStatus(2);
        friendRequestMapper.updateById(request);
    }
}