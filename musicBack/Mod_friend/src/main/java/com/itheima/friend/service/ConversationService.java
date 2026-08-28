package com.itheima.friend.service;

import com.itheima.friend.entity.FriendConversation;

import java.util.List;

/**
 * 好友会话服务接口
 */
public interface ConversationService {

    /**
     * 获取我的会话列表（含好友信息+未读数+最后消息）
     * <p>
     * 用于左栏好友列表渲染。
     */
    List<FriendConversation> getConversationList(Integer userId);

    /**
     * 获取或创建双向会话
     * <p>
     * 发送消息时调用：确保 A↔B 两条会话记录都存在。
     */
    void ensureConversations(Integer userId, Integer friendId);

    /**
     * 查询单条会话（调试用 / WS 重连补偿时）
     */
    FriendConversation getByUserAndFriend(Integer userId, Integer friendId);
}
