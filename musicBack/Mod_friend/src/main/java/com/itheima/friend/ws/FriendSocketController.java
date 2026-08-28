package com.itheima.friend.ws;

import com.itheima.friend.entity.FriendMessage;
import com.itheima.friend.service.MessageService;
import com.itheima.domain.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * 好友消息 STOMP 处理器
 * <p>
 * 客户端 → 服务端：/app/friend/message/send          发送消息（落库 + 推送）
 * 服务端 → 客户端：/user/{userId}/queue/friend/new    新消息推送（点对点）
 * <p>
 * 用 /user/{userId}/queue 而非 /topic 是因为消息是点对点的，
 * 只有接收者本人应该收到自己的新消息通知。
 */
@Controller
public class FriendSocketController {

    private static final Logger log = LoggerFactory.getLogger(FriendSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public FriendSocketController(SimpMessagingTemplate messagingTemplate,
                                  MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    /**
     * 通过 STOMP 发送消息（替代 HTTP POST /friend/message/send）
     * <p>
     * 客户端发送 SUBSCRIBE /user/{userId}/queue/friend/new 监听自己的新消息。
     * 发送成功后，服务端会向接收者推送，同时也向发送者推送一份（方便多设备同步）。
     *
     * @param dto       { friendId, content, msgType }
     * @param principal JWT 鉴权后的用户身份
     */
    @MessageMapping("/friend/message/send")
    public void sendMessage(@Payload Map<String, Object> dto,
                            Principal principal) {
        Integer senderId = userId(principal);
        if (senderId == null) {
            log.warn("STOMP sendMessage 未鉴权，忽略");
            return;
        }

        Integer friendId = dto.get("friendId") == null ? null : ((Number) dto.get("friendId")).intValue();
        String content = (String) dto.get("content");
        Integer msgType = dto.get("msgType") == null ? 0 : ((Number) dto.get("msgType")).intValue();

        if (friendId == null || content == null || content.isBlank()) {
            return;
        }

        try {
            FriendMessage msg = messageService.send(senderId, friendId, content, msgType);

            // 推送新消息给接收者（/user/{friendId}/queue/friend/new）
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(friendId),
                    "/queue/friend/new",
                    Result.success(msg)
            );

            // 同时推送给发送者自己（多设备同步）
            if (!senderId.equals(friendId)) {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(senderId),
                        "/queue/friend/new",
                        Result.success(msg)
                );
            }

            log.info("STOMP friend 消息发送 sender={} receiver={} id={}", senderId, friendId, msg.getId());
        } catch (Exception e) {
            log.warn("STOMP friend 消息发送失败: {}", e.getMessage());
            // 不推送错误给客户端，让 HTTP 接口作为兜底
        }
    }

    /**
     * 从 Principal 提取 userId
     */
    private Integer userId(Principal principal) {
        if (principal instanceof StompPrincipal sp) {
            return sp.getUserId();
        }
        return null;
    }
}
