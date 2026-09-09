package com.itheima.room.scheduler;

import com.itheima.room.ws.RoomPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 房间心跳巡检任务
 * <p>
 * 每 30 秒扫描一次：将超过 60 秒未心跳的成员标记下线并广播。
 */
@Component
@EnableScheduling
public class RoomHeartbeatScheduler {

    private static final Logger log = LoggerFactory.getLogger(RoomHeartbeatScheduler.class);

    private final RoomPresenceService presenceService;

    public RoomHeartbeatScheduler(RoomPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @Scheduled(fixedRate = 30000)
    public void scanOffline() {
        try {
            presenceService.heartbeatTick();
        } catch (Exception e) {
            log.error("心跳巡检异常", e);
        }
    }
}
