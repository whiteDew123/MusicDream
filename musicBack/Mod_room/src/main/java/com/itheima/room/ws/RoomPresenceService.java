package com.itheima.room.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.domain.entity.User;
import com.itheima.room.entity.RoomMember;
import com.itheima.room.mapper.RoomMemberMapper;
import com.itheima.room.mapper.UserMapper;
import com.itheima.room.vo.RoomMemberVO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间在线状态服务
 * <p>
 * 负责心跳更新、连接/断线标记、以及向 /topic/room/{id}/members 广播成员列表。
 */
@Service
public class RoomPresenceService {

    /** 心跳超时阈值（秒），超过则标记离线 */
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final RoomMemberMapper roomMemberMapper;
    private final UserMapper userMapper;
    private final RoomSessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomPresenceService(RoomMemberMapper roomMemberMapper,
                               UserMapper userMapper,
                               RoomSessionRegistry sessionRegistry,
                               SimpMessagingTemplate messagingTemplate) {
        this.roomMemberMapper = roomMemberMapper;
        this.userMapper = userMapper;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    /** 成员心跳：更新 last_heartbeat / is_online，并注册会话；仅"离线→在线"时有变化才广播，避免每次心跳都全量重查 member+user */
    public void memberOnline(Long roomId, Long userId, String sessionId) {
        if (sessionId != null) {
            sessionRegistry.put(sessionId, userId, roomId);
        }
        RoomMember member = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId)
                .eq(RoomMember::getUserId, userId));
        if (member == null) {
            return;
        }
        // 只在由离线变为在线时广播一次成员列表，心跳本身不再广播
        boolean wasOffline = member.getIsOnline() == null || member.getIsOnline() == 0;
        member.setLastHeartbeat(LocalDateTime.now());
        member.setIsOnline(1);
        roomMemberMapper.updateById(member);
        if (wasOffline) {
            broadcastPresence(roomId);
        }
    }

    /** 断线：移除会话并把该成员置为离线、广播 */
    public void memberOffline(String sessionId) {
        RoomSessionRegistry.SessionInfo info = sessionRegistry.remove(sessionId);
        if (info == null) {
            return;
        }
        RoomMember member = roomMemberMapper.selectOne(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, info.roomId)
                .eq(RoomMember::getUserId, info.userId));
        if (member != null) {
            member.setIsOnline(0);
            roomMemberMapper.updateById(member);
        }
        broadcastPresence(info.roomId);
    }

    /** 定时心跳巡检：超过 60 秒未心跳的成员标记离线并广播 */
    public void heartbeatTick() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        // 找出超时在线的成员
        List<RoomMember> offline = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getIsOnline, 1)
                .and(w -> w.isNull(RoomMember::getLastHeartbeat)
                        .or()
                        .lt(RoomMember::getLastHeartbeat, threshold)));
        if (offline.isEmpty()) {
            return;
        }
        List<Long> roomIds = new ArrayList<>();
        for (RoomMember m : offline) {
            m.setIsOnline(0);
            roomMemberMapper.updateById(m);
            if (!roomIds.contains(m.getRoomId())) {
                roomIds.add(m.getRoomId());
            }
        }
        for (Long roomId : roomIds) {
            broadcastPresence(roomId);
        }
    }

    /** 广播当前成员列表（含昵称/头像/在线态） */
    public void broadcastPresence(Long roomId) {
        List<RoomMember> members = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId)
                .orderByAsc(RoomMember::getRole)
                .orderByAsc(RoomMember::getJoinTime));
        List<Long> userIds = members.stream().map(RoomMember::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds.stream().map(Long::intValue).collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(User::getId, u -> u));

        List<RoomMemberVO> vos = new ArrayList<>();
        for (RoomMember m : members) {
            RoomMemberVO vo = new RoomMemberVO();
            vo.setUserId(m.getUserId());
            vo.setRole(m.getRole());
            vo.setIsOnline(m.getIsOnline());
            User u = userMap.get(m.getUserId().intValue());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setImageUrl(u.getImageUrl());
            }
            vos.add(vo);
        }
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/members", vos);
    }
}
