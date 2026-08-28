package com.itheima.friend.service;

import com.itheima.domain.entity.User;
import com.itheima.friend.entity.Friend;
import com.itheima.friend.entity.FriendRequest;

import java.util.List;

/**
 * 好友服务接口
 */
public interface FriendService {

    /**
     * 搜索用户（根据用户名模糊搜索，排除已好友）
     */
    List<User> searchUsers(Integer currentUserId, String keyword);

    /**
     * 发送好友请求
     */
    FriendRequest sendRequest(Integer senderId, Integer receiverId, String message);

    /**
     * 获取收到的好友请求
     */
    List<FriendRequest> getReceivedRequests(Integer receiverId);

    /**
     * 获取发送的好友请求
     */
    List<FriendRequest> getSentRequests(Integer senderId);

    /**
     * 接受好友请求
     */
    void acceptRequest(Integer requestId, Integer userId);

    /**
     * 拒绝好友请求
     */
    void rejectRequest(Integer requestId, Integer userId);

    /**
     * 获取好友列表
     */
    List<Friend> getFriendList(Integer userId);

    /**
     * 删除好友
     */
    void deleteFriend(Integer userId, Integer friendId);
}