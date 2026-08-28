package com.itheima.friend.service.impl;

import com.itheima.friend.entity.FriendConversation;
import com.itheima.friend.entity.FriendMessage;
import com.itheima.friend.mapper.FriendConversationMapper;
import com.itheima.friend.mapper.FriendMessageMapper;
import com.itheima.friend.service.ConversationService;
import com.itheima.friend.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 好友消息服务实现
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    /** 最后消息预览截断长度（会话表 last_message 字段 varchar(200)） */
    private static final int PREVIEW_MAX_LEN = 200;

    private final FriendMessageMapper messageMapper;
    private final FriendConversationMapper conversationMapper;
    private final ConversationService conversationService;

    public MessageServiceImpl(FriendMessageMapper messageMapper,
                              FriendConversationMapper conversationMapper,
                              ConversationService conversationService) {
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
        this.conversationService = conversationService;
    }

    @Override
    @Transactional
    public FriendMessage send(Integer senderId, Integer receiverId, String content, Integer msgType) {
        if (senderId == null || receiverId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发消息");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("消息内容不能为空");
        }
        if (msgType == null) {
            msgType = 0;
        }

        // 确保双向会话存在（顺便校验好友关系）
        conversationService.ensureConversations(senderId, receiverId);

        // 拿发送方方向的会话ID关联消息（A→B 会话）
        FriendConversation senderConv = conversationMapper.selectByUserAndFriend(senderId, receiverId);
        if (senderConv == null) {
            throw new RuntimeException("会话创建失败");
        }

        // 写消息
        FriendMessage msg = new FriendMessage();
        msg.setConversationId(senderConv.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setIsRead(0);
        msg.setCreateTime(new Date());
        messageMapper.insert(msg);

        // 截断内容做预览
        String preview = content.length() > PREVIEW_MAX_LEN
                ? content.substring(0, PREVIEW_MAX_LEN) + "..."
                : content;

        // 更新发送方会话（最后消息，未读数不变）
        conversationMapper.updateLastMessage(senderId, receiverId, preview);

        // 更新接收方会话（最后消息 + 未读数 +1）
        conversationMapper.updateLastMessageAndUnread(receiverId, senderId, preview);

        return msg;
    }

    @Override
    public List<FriendMessage> getHistory(Integer userId, Integer friendId, Long before, Integer size) {
        // 确保会话存在（顺带校验好友关系）
        conversationService.ensureConversations(userId, friendId);

        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        // 按用户对双向查询（不依赖 conversation_id，避免方向匹配问题）
        List<FriendMessage> list = messageMapper.selectMessageHistory(userId, friendId, before, pageSize);

        // 倒序翻正：游标分页按 id DESC 拉取（最新在前），前端展示需要时间正序
        java.util.Collections.reverse(list);
        return list;
    }

    @Override
    @Transactional
    public void markRead(Integer userId, Integer friendId) {
        // 批量标记对方发给我的未读消息为已读
        messageMapper.updateReadStatus(userId, friendId);
        // 清零会话未读数
        conversationMapper.resetUnread(userId, friendId);
    }

    @Override
    public int countAllUnread(Integer userId) {
        return messageMapper.countAllUnread(userId);
    }
}
