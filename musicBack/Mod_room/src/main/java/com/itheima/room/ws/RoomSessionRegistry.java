package com.itheima.room.ws;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * STOMP 会话注册表
 * <p>
 * 记录 STOMP sessionId 对应的 (userId, roomId)，用于：
 * - 断线时据此把成员标记离线并广播
 * - 心跳/同步消息到达时更新在线态
 */
@Component
public class RoomSessionRegistry {

    /** sessionId -> 会话信息 */
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    /** 会话信息 */
    public static class SessionInfo {
        public final Long userId;
        public final Long roomId;

        public SessionInfo(Long userId, Long roomId) {
            this.userId = userId;
            this.roomId = roomId;
        }
    }

    /** 注册（或更新）一次会话 */
    public void put(String sessionId, Long userId, Long roomId) {
        sessions.put(sessionId, new SessionInfo(userId, roomId));
    }

    /** 读取会话信息 */
    public SessionInfo get(String sessionId) {
        return sessions.get(sessionId);
    }

    /** 移除会话 */
    public SessionInfo remove(String sessionId) {
        return sessions.remove(sessionId);
    }
}
