package com.itheima.room.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.domain.entity.User;
import com.itheima.room.entity.RoomMember;
import com.itheima.room.mapper.RoomMemberMapper;
import com.itheima.room.mapper.RoomMemberSessionMapper;
import com.itheima.room.mapper.RoomStatsMapper;
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
    private final RoomMemberSessionMapper sessionMapper;
    private final RoomStatsMapper statsMapper;

    public RoomPresenceService(RoomMemberMapper roomMemberMapper,
                               UserMapper userMapper,
                               RoomSessionRegistry sessionRegistry,
                               SimpMessagingTemplate messagingTemplate,
                               RoomMemberSessionMapper sessionMapper,
                               RoomStatsMapper statsMapper) {
        this.roomMemberMapper = roomMemberMapper;
        this.userMapper = userMapper;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
        this.sessionMapper = sessionMapper;
        this.statsMapper = statsMapper;
    }

    /** 成员心跳：更新 last_heartbeat / is_online，并注册会话 */
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
        boolean wasOffline = member.getIsOnline() == null || member.getIsOnline() == 0;
        member.setLastHeartbeat(LocalDateTime.now());
        member.setIsOnline(1);
        roomMemberMapper.updateById(member);
        if (wasOffline) {
            broadcastPresence(roomId);
            // 离线→在线：更新 peak_online
            refreshPeak(roomId);
        }
    }

    /** 断线：移除会话、置离线、关闭 session、广播 */
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
        // 关闭这个用户的未关闭 session
        sessionMapper.closeSession(info.roomId, info.userId, LocalDateTime.now());
        broadcastPresence(info.roomId);
    }

    /** 定时心跳巡检：超时成员置离线 + 关闭 session + 刷新所有活跃房间的峰值和累计分钟 */
    public void heartbeatTick() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        List<RoomMember> offline = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getIsOnline, 1)
                .and(w -> w.isNull(RoomMember::getLastHeartbeat)
                        .or()
                        .lt(RoomMember::getLastHeartbeat, threshold)));

        // 1) 处理离线：收集每个房间的离线用户
        if (!offline.isEmpty()) {
            Map<Long, List<Long>> roomUserMap = offline.stream()
                    .collect(Collectors.groupingBy(RoomMember::getRoomId,
                            Collectors.mapping(RoomMember::getUserId, Collectors.toList())));

            LocalDateTime now = LocalDateTime.now();
            for (RoomMember m : offline) {
                m.setIsOnline(0);
                roomMemberMapper.updateById(m);
            }

            // 批量关闭离线成员的未关闭 session
            for (Map.Entry<Long, List<Long>> e : roomUserMap.entrySet()) {
                sessionMapper.closeSessions(e.getKey(), e.getValue(), now);
                broadcastPresence(e.getKey());
            }
        }

        // 2) 兜底：不管有没有人离线，每 30s 扫一遍所有活跃房间 → 刷新峰值 + 重算累计分钟
        //    这样新人加入、心跳超时、正常离开三条路径都能覆盖到
        List<Long> activeRoomIds = roomMemberMapper.selectList(new LambdaQueryWrapper<RoomMember>()
                        .select(RoomMember::getRoomId)
                        .eq(RoomMember::getIsOnline, 1)
                        .groupBy(RoomMember::getRoomId))
                .stream().map(RoomMember::getRoomId).distinct().collect(Collectors.toList());
        for (Long roomId : activeRoomIds) {
            refreshPeak(roomId);
            statsMapper.recalcTotalWatchMinutes(roomId);
        }
    }

    /** 刷新指定房间的峰值在线数（public，供 joinRoom 等外部路径调用） */
    public void refreshPeak(Long roomId) {
        int onlineCount = Math.toIntExact(roomMemberMapper.selectCount(new LambdaQueryWrapper<RoomMember>()
                .eq(RoomMember::getRoomId, roomId)
                .eq(RoomMember::getIsOnline, 1)));
        if (onlineCount == 0) return;
        statsMapper.updatePeakIfHigher(roomId, onlineCount);
    }

    /** 广播当前成员列表 */
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
