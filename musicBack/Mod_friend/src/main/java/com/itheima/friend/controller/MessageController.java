package com.itheima.friend.controller;

import com.itheima.domain.common.Result;
import com.itheima.friend.entity.FriendConversation;
import com.itheima.friend.entity.FriendMessage;
import com.itheima.friend.service.ConversationService;
import com.itheima.friend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友消息接口
 * <p>
 * 网关路由：/api/friend/** → StripPrefix=1 → /friend/**
 * <p>
 * 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
 */
@RestController
@RequestMapping("/friend")
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    public MessageController(MessageService messageService,
                             ConversationService conversationService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
    }

    // ======================== 会话 ========================

    /**
     * 获取会话列表（用于左栏好友列表渲染：最后消息+未读数）
     * <p>
     * GET /friend/conversations
     */
    @GetMapping("/conversations")
    public Result<List<FriendConversation>> getConversationList(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(conversationService.getConversationList(userId));
    }

    // ======================== 消息 ========================

    /**
     * 发送消息
     * <p>
     * POST /friend/message/send
     * body: { friendId: 2, content: "你好", msgType: 0 }
     */
    @PostMapping("/message/send")
    public Result<FriendMessage> sendMessage(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Integer friendId = body.get("friendId") == null ? null : ((Number) body.get("friendId")).intValue();
        String content = (String) body.get("content");
        Integer msgType = body.get("msgType") == null ? 0 : ((Number) body.get("msgType")).intValue();

        if (friendId == null) {
            return Result.error(400, "缺少 friendId 参数");
        }
        if (content == null || content.isBlank()) {
            return Result.error(400, "消息内容不能为空");
        }

        FriendMessage msg = messageService.send(userId, friendId, content, msgType);
        return Result.success("发送成功", msg);
    }

    /**
     * 拉取历史消息（游标分页）
     * <p>
     * GET /friend/message/history/{friendId}?before=123&size=20
     * before 为 null 时拉最新一页；否则拉 id < before 的更早消息。
     */
    @GetMapping("/message/history/{friendId}")
    public Result<List<FriendMessage>> getHistory(
            @PathVariable Integer friendId,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Integer size,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<FriendMessage> list = messageService.getHistory(userId, friendId, before, size);
        return Result.success(list);
    }

    /**
     * 标记某会话已读（批量标记对方发给我的消息 + 清零会话未读数）
     * <p>
     * POST /friend/message/read/{friendId}
     */
    @PostMapping("/message/read/{friendId}")
    public Result<Void> markRead(
            @PathVariable Integer friendId,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markRead(userId, friendId);
        return Result.success();
    }

    /**
     * 获取我所有未读消息数（用于顶部 Badge 角标）
     * <p>
     * GET /friend/message/unread-count
     */
    @GetMapping("/message/unread-count")
    public Result<Integer> countAllUnread(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(messageService.countAllUnread(userId));
    }
}
