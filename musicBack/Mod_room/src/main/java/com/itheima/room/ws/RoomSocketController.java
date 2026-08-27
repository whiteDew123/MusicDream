package com.itheima.room.ws;

import com.itheima.room.dto.SkipVoteDTO;
import com.itheima.room.entity.Room;
import com.itheima.room.mapper.RoomMapper;
import com.itheima.room.service.RoomMessageService;
import com.itheima.room.service.RoomVoteService;
import com.itheima.room.vo.RoomMessageVO;
import com.itheima.room.vo.RoomVoteVO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 播放室 STOMP 消息处理器
 * <p>
 * 客户端 → 服务端：/app/room/{id}/sync | heartbeat | chat | skip-vote | skip-vote/agree
 * 服务端 → 客户端：/topic/room/{id}/state | members | message | skip-vote
 */
@Controller
public class RoomSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomMapper roomMapper;
    private final RoomMessageService roomMessageService;
    private final RoomVoteService roomVoteService;
    private final RoomPresenceService presenceService;
    private final RoomNotifier roomNotifier;

    public RoomSocketController(SimpMessagingTemplate messagingTemplate,
                                RoomMapper roomMapper,
                                RoomMessageService roomMessageService,
                                RoomVoteService roomVoteService,
                                RoomPresenceService presenceService,
                                RoomNotifier roomNotifier) {
        this.messagingTemplate = messagingTemplate;
        this.roomMapper = roomMapper;
        this.roomMessageService = roomMessageService;
        this.roomVoteService = roomVoteService;
        this.presenceService = presenceService;
        this.roomNotifier = roomNotifier;
    }

    /** 房主上报播放状态并广播 */
    @MessageMapping("/room/{roomId}/sync")
    public void sync(@DestinationVariable Long roomId,
                     @Payload RoomSyncDTO dto,
                     Principal principal,
                     @Header("simpSessionId") String sessionId) {
        Long userId = userId(principal);
        if (userId == null) {
            return;
        }
        Room room = roomMapper.selectById(roomId);
        // 仅房主可上报播放状态
        if (room == null || !userId.equals(room.getOwnerId())) {
            return;
        }
        if (dto.getMusicId() != null) {
            room.setCurrentMusicId(dto.getMusicId());
            room.setStatus(1);
        }
        if (dto.getProgress() != null) {
            room.setCurrentProgress(dto.getProgress());
        }
        if (dto.getIsPlaying() != null) {
            room.setIsPlaying(dto.getIsPlaying());
            room.setStatus(dto.getIsPlaying() == 1 ? 1 : room.getStatus());
        }
        roomMapper.updateById(room);
        presenceService.memberOnline(roomId, userId, sessionId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/state", toState(room));
    }

    /** 成员心跳（含房主） */
    @MessageMapping("/room/{roomId}/heartbeat")
    public void heartbeat(@DestinationVariable Long roomId,
                          Principal principal,
                          @Header("simpSessionId") String sessionId) {
        Long userId = userId(principal);
        if (userId == null) {
            return;
        }
        presenceService.memberOnline(roomId, userId, sessionId);
    }

    /** 聊天消息（落库 + 广播） */
    @MessageMapping("/room/{roomId}/chat")
    public void chat(@DestinationVariable Long roomId,
                     @Payload RoomChatDTO dto,
                     Principal principal,
                     @Header("simpSessionId") String sessionId) {
        Long userId = userId(principal);
        if (userId == null || dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
            return;
        }
        presenceService.memberOnline(roomId, userId, sessionId);
        RoomMessageVO msg = roomMessageService.send(roomId, userId, dto.getType(), dto.getContent());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/message", msg);
    }

    /** 发起切歌投票 */
    @MessageMapping("/room/{roomId}/skip-vote")
    public void skipVote(@DestinationVariable Long roomId,
                         @Payload SkipVoteDTO dto,
                         Principal principal,
                         @Header("simpSessionId") String sessionId) {
        Long userId = userId(principal);
        if (userId == null || dto == null || dto.getMusicId() == null) {
            return;
        }
        presenceService.memberOnline(roomId, userId, sessionId);
        roomVoteService.skipVote(roomId, dto.getMusicId(), userId);
        RoomVoteVO state = roomVoteService.getVoteState(roomId, dto.getMusicId());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/skip-vote",
                Map.of("musicId", dto.getMusicId(), "action", "init", "stats", state));
    }

    /** 附议切歌投票 */
    @MessageMapping("/room/{roomId}/skip-vote/agree")
    public void agreeVote(@DestinationVariable Long roomId,
                          @Payload SkipVoteDTO dto,
                          Principal principal,
                          @Header("simpSessionId") String sessionId) {
        Long userId = userId(principal);
        if (userId == null || dto == null || dto.getMusicId() == null) {
            return;
        }
        presenceService.memberOnline(roomId, userId, sessionId);
        boolean skipped = roomVoteService.agreeVote(roomId, dto.getMusicId(), userId);
        if (skipped) {
            Room room = roomMapper.selectById(roomId);
            if (room != null) {
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/state", toState(room));
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/skip-vote",
                        Map.of("musicId", room.getCurrentMusicId(), "action", "passed"));
                // 系统消息：切歌成功
                roomNotifier.systemMessage(roomId, null, "附议通过，已切至下一首");
            }
        } else {
            RoomVoteVO state = roomVoteService.getVoteState(roomId, dto.getMusicId());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/skip-vote",
                    Map.of("musicId", dto.getMusicId(), "action", "agree", "stats", state));
        }
    }

    /** 从 Principal 提取 userId */
    private Long userId(Principal principal) {
        if (principal instanceof StompPrincipal sp) {
            return sp.getUserId();
        }
        return null;
    }

    /** 组装状态广播消息 */
    private RoomStateMessage toState(Room room) {
        RoomStateMessage msg = new RoomStateMessage();
        msg.setMusicId(room.getCurrentMusicId());
        msg.setProgress(room.getCurrentProgress());
        msg.setIsPlaying(room.getIsPlaying());
        msg.setStatus(room.getStatus());
        msg.setAt(LocalDateTime.now());
        return msg;
    }
}
