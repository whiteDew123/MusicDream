package com.itheima.room.ws;

import com.itheima.room.service.RoomMessageService;
import com.itheima.room.vo.RoomDetailVO;
import com.itheima.room.vo.RoomMessageVO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 房间通知器
 * <p>
 * 用于在加入/离开/移出/关闭/切歌等事件时生成一条系统消息（type=2）并广播给房间成员，
 * 以及房间设置变更时广播最新房间详情。
 */
@Component
public class RoomNotifier {

    private final RoomMessageService roomMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomNotifier(RoomMessageService roomMessageService,
                        SimpMessagingTemplate messagingTemplate) {
        this.roomMessageService = roomMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送并广播一条系统消息
     *
     * @param roomId  房间ID
     * @param userId  相关用户（可为 null，例如"房间已关闭"）
     * @param content 消息内容
     */
    public void systemMessage(Long roomId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        RoomMessageVO msg = roomMessageService.send(roomId, userId, 2, content);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/message", msg);
    }

    /**
     * 广播房间详情更新（名称、人数上限、播放模式等变更时调用）
     */
    public void roomUpdated(RoomDetailVO detail) {
        if (detail == null || detail.getId() == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/room/" + detail.getId() + "/room", detail);
    }

    /**
     * 广播房间已关闭信号（硬删除前调用），让所有成员端自动跳回房间列表
     */
    public void roomClosed(Long roomId) {
        if (roomId == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/closed", Map.of("closed", true));
    }
}