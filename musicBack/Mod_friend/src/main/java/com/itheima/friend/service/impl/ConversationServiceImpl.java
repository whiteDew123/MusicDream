package com.itheima.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.friend.entity.Friend;
import com.itheima.friend.entity.FriendConversation;
import com.itheima.friend.entity.FriendMessage;
import com.itheima.friend.mapper.FriendConversationMapper;
import com.itheima.friend.mapper.FriendMapper;
import com.itheima.friend.mapper.FriendMessageMapper;
import com.itheima.friend.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 好友会话服务实现
 * <p>
 * 会话表是物化视图：写入消息时多一次更新，换取列表查询时一查即出。
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    private final FriendConversationMapper conversationMapper;
    private final FriendMapper friendMapper;

    public ConversationServiceImpl(FriendConversationMapper conversationMapper,
                                   FriendMapper friendMapper) {
        this.conversationMapper = conversationMapper;
        this.friendMapper = friendMapper;
    }

    @Override
    public List<FriendConversation> getConversationList(Integer userId) {
        return conversationMapper.selectConversationList(userId);
    }

    @Override
    @Transactional
    public void ensureConversations(Integer userId, Integer friendId) {
        // 校验好友关系
        int count = friendMapper.countFriend(userId, friendId);
        if (count == 0) {
            throw new RuntimeException("不是好友，无法发送消息");
        }

        // 检查/创建 userId → friendId 方向
        FriendConversation c1 = conversationMapper.selectByUserAndFriend(userId, friendId);
        if (c1 == null) {
            c1 = new FriendConversation();
            c1.setUserId(userId);
            c1.setFriendId(friendId);
            c1.setLastMessage("");
            c1.setUnreadCount(0);
            c1.setLastMsgTime(new Date());
            conversationMapper.insert(c1);
        }

        // 检查/创建 friendId → userId 方向
        FriendConversation c2 = conversationMapper.selectByUserAndFriend(friendId, userId);
        if (c2 == null) {
            c2 = new FriendConversation();
            c2.setUserId(friendId);
            c2.setFriendId(userId);
            c2.setLastMessage("");
            c2.setUnreadCount(0);
            c2.setLastMsgTime(new Date());
            conversationMapper.insert(c2);
        }
    }

    @Override
    public FriendConversation getByUserAndFriend(Integer userId, Integer friendId) {
        return conversationMapper.selectByUserAndFriend(userId, friendId);
    }
}
