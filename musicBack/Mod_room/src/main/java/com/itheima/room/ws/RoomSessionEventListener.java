package com.itheima.room.ws;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * STOMP 会话事件监听
 * <p>
 * 监听断线事件，把对应成员标记离线并广播成员列表。
 */
@Component
public class RoomSessionEventListener {

    private final RoomPresenceService presenceService;

    public RoomSessionEventListener(RoomPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    /** 客户端 STOMP 断线 */
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        presenceService.memberOffline(event.getSessionId());
    }
}
