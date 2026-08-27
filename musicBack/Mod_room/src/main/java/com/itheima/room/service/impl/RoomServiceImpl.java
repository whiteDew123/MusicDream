package com.itheima.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import com.itheima.room.dto.RoomCreateDTO;
import com.itheima.room.dto.RoomUpdateDTO;
import com.itheima.room.entity.Room;
import com.itheima.room.entity.RoomMember;
import com.itheima.room.entity.RoomMessage;
import com.itheima.room.entity.RoomPlaylist;
import com.itheima.room.entity.RoomPlaylistVote;
import com.itheima.room.mapper.MusicMapper;
import com.itheima.room.mapper.RoomMapper;
import com.itheima.room.mapper.RoomMemberMapper;
import com.itheima.room.mapper.RoomMessageMapper;
import com.itheima.room.mapper.RoomPlaylistMapper;
import com.itheima.room.mapper.RoomPlaylistVoteMapper;
import com.itheima.room.mapper.UserMapper;
import com.itheima.room.service.RoomService;
import com.itheima.room.vo.RoomDetailVO;
import com.itheima.room.vo.RoomMemberVO;
import com.itheima.room.vo.RoomVO;
import com.itheima.room.ws.RoomNotifier;
import com.itheima.room.ws.RoomPresenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 房间 Service 实现
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final RoomMemberMapper roomMemberMapper;
    private final MusicMapper musicMapper;
    private final UserMapper userMapper;
    private final RoomPlaylistMapper roomPlaylistMapper;
    private final RoomPlaylistVoteMapper roomPlaylistVoteMapper;
    private final RoomMessageMapper roomMessageMapper;
    private final RoomNotifier roomNotifier;
    private final RoomPresenceService presenceService;

    public RoomServiceImpl(RoomMemberMapper roomMemberMapper,
                           MusicMapper musicMapper,
                           UserMapper userMapper,
                           RoomPlaylistMapper roomPlaylistMapper,
                           RoomPlaylistVoteMapper roomPlaylistVoteMapper,
                           RoomMessageMapper roomMessageMapper,
                           RoomNotifier roomNotifier,
                           RoomPresenceService presenceService) {
        this.roomMemberMapper = roomMemberMapper;
        this.musicMapper = musicMapper;
        this.userMapper = userMapper;
        this.roomPlaylistMapper = roomPlaylistMapper;
        this.roomPlaylistVoteMapper = roomPlaylistVoteMapper;
        this.roomMessageMapper = roomMessageMapper;
        this.roomNotifier = roomNotifier;
        this.presenceService = presenceService;
    }

    /** 生成邀请码用的安全随机数（排除易混淆字符 0/O/1/I） */
    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomDetailVO create(RoomCreateDTO dto, Long userId) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("房间名称不能为空");
        }
        int maxMembers = dto.getMaxMembers() != null ? dto.getMaxMembers() : 5;
        if (maxMembers < 2) {
            throw new IllegalArgumentException("人数上限至少为2");
        }
        if (maxMembers > 5) {
            throw new IllegalArgumentException("人数上限最大为5");
        }
        int expire = dto.getInviteExpireHours() != null ? dto.getInviteExpireHours() : 6;
        if (expire < 1 || expire > 12) {
            throw new IllegalArgumentException("邀请码有效期需在1-12小时之间");
        }

        Room room = new Room();
        room.setName(dto.getName().trim());
        room.setOwnerId(userId);
        room.setMaxMembers(maxMembers);
        room.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()) ? 1 : 0);
        room.setInviteCode(generateInviteCode());
        room.setStatus(0);
        room.setCurrentProgress(0.0);
        room.setIsPlaying(0);
        room.setPlayMode(dto.getPlayMode() != null ? dto.getPlayMode() : 0);
        room.setInviteExpireHours(expire);
        room.setCreateTime(LocalDateTime.now());
        save(room);

        // 创建者自动成为房主并加入房间
        RoomMember member = new RoomMember();
        member.setRoomId(room.getId());
        member.setUserId(userId);
        member.setRole(0);
        member.setIsOnline(1);
        member.setJoinTime(LocalDateTime.now());
        roomMemberMapper.insert(member);

        return getDetail(room.getId(), userId);
    }

    @Override
    public RoomDetailVO getDetail(Long id, Long userId) {
        Room room = getById(id);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        return toDetail(room, userId);
    }

    @Override
    public List<RoomVO> listRooms(Long userId) {
        // 公开且未关闭的房间 + 我作为成员加入的房间（含私密）
        List<Room> activeRooms = list(new LambdaQueryWrapper<Room>()
                .ne(Room::getStatus, 2));

        List<Long> myRoomIds = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                        .eq(RoomMember::getUserId, userId)).stream()
                .map(RoomMember::getRoomId)
                .collect(Collectors.toList());

        List<Room> visible = activeRooms.stream()
                .filter(r -> Objects.equals(r.getIsPublic(), 1) || myRoomIds.contains(r.getId()))
                .collect(Collectors.toList());
        // 新建的房间排前面
        visible.sort((a, b) -> b.getId().compareTo(a.getId()));

        return visible.stream().map(r -> toVO(r, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomDetailVO update(RoomUpdateDTO dto, Long userId) {
        Room room = getById(dto.getId());
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        requireOwner(room, userId);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            room.setName(dto.getName().trim());
        }
        if (dto.getMaxMembers() != null) {
            if (dto.getMaxMembers() < 2 || dto.getMaxMembers() > 5) {
                throw new IllegalArgumentException("人数上限需在2-5之间");
            }
            room.setMaxMembers(dto.getMaxMembers());
        }
        if (dto.getIsPublic() != null) {
            room.setIsPublic(dto.getIsPublic() ? 1 : 0);
        }
        if (dto.getInviteExpireHours() != null) {
            if (dto.getInviteExpireHours() < 1 || dto.getInviteExpireHours() > 12) {
                throw new IllegalArgumentException("邀请码有效期需在1-12小时之间");
            }
            room.setInviteExpireHours(dto.getInviteExpireHours());
        }
        if (dto.getCover() != null) {
            room.setCover(dto.getCover());
        }
        if (dto.getPlayMode() != null) {
            if (dto.getPlayMode() != 0 && dto.getPlayMode() != 1) {
                throw new IllegalArgumentException("播放模式不合法");
            }
            room.setPlayMode(dto.getPlayMode());
        }
        updateById(room);
        RoomDetailVO detail = getDetail(dto.getId(), userId);
        roomNotifier.roomUpdated(detail);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id, Long userId) {
        Room room = getById(id);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        requireOwner(room, userId);
        doClose(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomDetailVO join(Long id, Long userId) {
        Room room = getById(id);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        if (Objects.equals(room.getStatus(), 2)) {
            throw new IllegalArgumentException("房间已关闭");
        }

        // 重复加入同一房间：直接返回详情（先判断，避免不必要的退出其它房间）
        RoomMember existing = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, id)
                .eq(RoomMember::getUserId, userId));
        if (existing != null) {
            return getDetail(id, userId);
        }

        // 一个用户同时只能在一个房间：自动退出其它房间
        List<RoomMember> otherMembers = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getUserId, userId)
                .ne(RoomMember::getRoomId, id));
        for (RoomMember other : otherMembers) {
            leaveRoom(other.getRoomId(), userId);
        }

        // 人数上限校验
        long count = roomMemberMapper.selectCount(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, id));
        if (count >= room.getMaxMembers()) {
            throw new IllegalArgumentException("房间已满");
        }

        RoomMember member = new RoomMember();
        member.setRoomId(id);
        member.setUserId(userId);
        member.setRole(1);
        member.setIsOnline(1);
        member.setJoinTime(LocalDateTime.now());
        roomMemberMapper.insert(member);
        roomNotifier.systemMessage(id, userId, "加入了房间");
        presenceService.broadcastPresence(id);

        return getDetail(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long id, Long userId) {
        leaveRoom(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long id, Long userId, Long targetUserId) {
        Room room = getById(id);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        requireOwner(room, userId);
        if (targetUserId == null || targetUserId.equals(userId)) {
            throw new IllegalArgumentException("转让对象不合法");
        }
        RoomMember target = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, id)
                .eq(RoomMember::getUserId, targetUserId));
        if (target == null) {
            throw new IllegalArgumentException("该成员不在房间中");
        }

        // 乐观锁：仅当 owner_id 仍为当前房主时才更新，防止转让与退出并发
        boolean ok = update(new LambdaUpdateWrapper<Room>()
                .eq(Room::getId, id)
                .eq(Room::getOwnerId, userId)
                .set(Room::getOwnerId, targetUserId));
        if (!ok) {
            throw new IllegalStateException("转让失败，房间状态已变更，请重试");
        }
        // 更新成员角色：原房主降为成员，目标成员升为房主
        RoomMember ownerMember = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, id)
                .eq(RoomMember::getUserId, userId)
                .eq(RoomMember::getRole, 0));
        if (ownerMember != null) {
            ownerMember.setRole(1);
            roomMemberMapper.updateById(ownerMember);
        }
        target.setRole(0);
        roomMemberMapper.updateById(target);

        // 广播：成员角色变化 + 房主变更 + 系统消息，让全员实时感知（transfer 热更新）
        presenceService.broadcastPresence(id);
        roomNotifier.roomUpdated(getDetail(id, null));
        roomNotifier.systemMessage(id, targetUserId, "已成为新房主");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kick(Long id, Long userId, Long targetUserId) {
        Room room = getById(id);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        requireOwner(room, userId);
        if (targetUserId == null || targetUserId.equals(userId)) {
            throw new IllegalArgumentException("不能移出自己");
        }
        RoomMember target = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, id)
                .eq(RoomMember::getUserId, targetUserId));
        if (target == null) {
            throw new IllegalArgumentException("该成员不在房间中");
        }
        if (Objects.equals(target.getRole(), 0)) {
            throw new IllegalArgumentException("不能移出房主");
        }
        roomMemberMapper.deleteById(target.getId());
        roomNotifier.systemMessage(id, targetUserId, "被移出房间");
        presenceService.broadcastPresence(id);
    }

    @Override
    public RoomDetailVO getByInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("邀请码不能为空");
        }
        Room room = getOne(new LambdaQueryWrapper<Room>()
                .eq(Room::getInviteCode, inviteCode.trim()));
        if (room == null) {
            throw new IllegalArgumentException("邀请码无效或房间不存在");
        }
        if (Objects.equals(room.getStatus(), 2)) {
            throw new IllegalArgumentException("房间已关闭");
        }
        // 邀请码有效期校验
        if (room.getCreateTime() != null && room.getInviteExpireHours() != null) {
            LocalDateTime expire = room.getCreateTime().plusHours(room.getInviteExpireHours());
            if (LocalDateTime.now().isAfter(expire)) {
                throw new IllegalArgumentException("邀请码已过期");
            }
        }
        // 公开落地页：未登录查看，userId 传 null
        return toDetail(room, null);
    }

    // ======================== 私有辅助方法 ========================

    /** 校验当前用户是否为房主 */
    private void requireOwner(Room room, Long userId) {
        if (room.getOwnerId() == null || !room.getOwnerId().equals(userId)) {
            throw new IllegalStateException("仅房主可执行该操作");
        }
    }

    /** 离开房间（拆分为独立方法，供自动退出其它房间复用） */
    private void leaveRoom(Long roomId, Long userId) {
        RoomMember member = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId)
                .eq(RoomMember::getUserId, userId));
        if (member == null) {
            throw new IllegalArgumentException("你不在该房间中");
        }
        // 房主离开：房间解散
        if (Objects.equals(member.getRole(), 0)) {
            doClose(roomId);
            return;
        }
        roomMemberMapper.deleteById(member.getId());
        roomNotifier.systemMessage(roomId, userId, "离开了房间");
        presenceService.broadcastPresence(roomId);
        // 最后一人离开：房间关闭
        long remaining = roomMemberMapper.selectCount(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId));
        if (remaining == 0) {
            doClose(roomId);
        }
    }

    /** 真正关闭：置状态为已结束并清空成员，同时在状态真正变更时广播系统消息 */
    private void doClose(Long roomId) {
        roomNotifier.systemMessage(roomId, null, "房间已关闭");
        roomNotifier.roomClosed(roomId);
        presenceService.broadcastPresence(roomId);
        roomMemberMapper.delete(new LambdaQueryWrapper<RoomMember>().eq(RoomMember::getRoomId, roomId));
        roomPlaylistMapper.delete(new LambdaQueryWrapper<RoomPlaylist>().eq(RoomPlaylist::getRoomId, roomId));
        roomPlaylistVoteMapper.delete(new LambdaQueryWrapper<RoomPlaylistVote>().eq(RoomPlaylistVote::getRoomId, roomId));
        roomMessageMapper.delete(new LambdaQueryWrapper<RoomMessage>().eq(RoomMessage::getRoomId, roomId));
        removeById(roomId);
    }

    /** 生成 8 位不重复邀请码 */
    private String generateInviteCode() {
        SecureRandom random = new SecureRandom();
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(INVITE_CHARS.charAt(random.nextInt(INVITE_CHARS.length())));
            }
            String code = sb.toString();
            long exists = count(new LambdaQueryWrapper<Room>().eq(Room::getInviteCode, code));
            if (exists == 0) {
                return code;
            }
        }
        throw new IllegalStateException("邀请码生成失败，请重试");
    }

    /** 房间 -> 基础 VO */
    private RoomVO toVO(Room room, Long userId) {
        RoomVO vo = new RoomVO();
        vo.setId(room.getId());
        vo.setName(room.getName());
        vo.setOwnerId(room.getOwnerId());
        vo.setMaxMembers(room.getMaxMembers());
        vo.setCover(room.getCover());
        vo.setIsPublic(room.getIsPublic());
        vo.setInviteCode(room.getInviteCode());
        vo.setStatus(room.getStatus());
        vo.setCurrentMusicId(room.getCurrentMusicId());
        vo.setCurrentProgress(room.getCurrentProgress());
        vo.setIsPlaying(room.getIsPlaying());
        vo.setPlayMode(room.getPlayMode());
        vo.setInviteExpireHours(room.getInviteExpireHours());
        vo.setCreateTime(room.getCreateTime());
        vo.setOwnerName(fetchUsername(room.getOwnerId()));
        vo.setMemberCount(roomMemberMapper.selectCount(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, room.getId())));
        if (userId != null) {
            vo.setIsOwner(room.getOwnerId() != null && room.getOwnerId().equals(userId));
            RoomMember mine = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                    .eq(RoomMember::getRoomId, room.getId())
                    .eq(RoomMember::getUserId, userId));
            vo.setIsMember(mine != null);
        } else {
            vo.setIsOwner(false);
            vo.setIsMember(false);
        }
        return vo;
    }

    /** 房间 -> 详情 VO（含成员列表与当前歌曲信息） */
    private RoomDetailVO toDetail(Room room, Long userId) {
        RoomDetailVO vo = new RoomDetailVO();
        RoomVO base = toVO(room, userId);
        // 复制基础字段（借助 BeanUtils 减少手写）
        org.springframework.beans.BeanUtils.copyProperties(base, vo);

        List<RoomMember> members = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, room.getId())
                .orderByAsc(RoomMember::getRole)
                .orderByAsc(RoomMember::getJoinTime));
        List<Long> userIds = members.stream().map(RoomMember::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, User> userMap = loadUserMap(userIds);
        List<RoomMemberVO> memberVOs = new ArrayList<>();
        for (RoomMember m : members) {
            RoomMemberVO mvo = new RoomMemberVO();
            mvo.setUserId(m.getUserId());
            User u = userMap.get(m.getUserId().intValue());
            if (u != null) {
                mvo.setUsername(u.getUsername());
                mvo.setImageUrl(u.getImageUrl());
            }
            mvo.setRole(m.getRole());
            mvo.setIsOnline(m.getIsOnline());
            memberVOs.add(mvo);
        }
        vo.setMembers(memberVOs);

        // 当前歌曲展示信息
        if (room.getCurrentMusicId() != null) {
            Music music = musicMapper.selectById(room.getCurrentMusicId().intValue());
            if (music != null) {
                vo.setCurrentMusicName(music.getMusicName());
                vo.setCurrentMusicCover(music.getImageUrl());
            }
        }
        return vo;
    }

    /** 批量加载用户，返回 userId(Integer) -> User */
    private Map<Integer, User> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> ids = userIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    /** 取单个用户昵称 */
    private String fetchUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId.intValue());
        return user == null ? null : user.getUsername();
    }
}