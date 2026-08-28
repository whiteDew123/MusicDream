package com.itheima.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.domain.entity.User;
import com.itheima.friend.entity.Friend;
import com.itheima.friend.entity.FriendRequest;
import com.itheima.friend.mapper.FriendMapper;
import com.itheima.friend.mapper.FriendRequestMapper;
import com.itheima.friend.mapper.UserSearchMapper;
import com.itheima.friend.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 好友服务实现类
 */
@Service
public class FriendServiceImpl implements FriendService {

    private static final int MAX_FRIENDS = 300;

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserSearchMapper userSearchMapper;

    public FriendServiceImpl(FriendMapper friendMapper,
                             FriendRequestMapper friendRequestMapper,
                             UserSearchMapper userSearchMapper) {
        this.friendMapper = friendMapper;
        this.friendRequestMapper = friendRequestMapper;
        this.userSearchMapper = userSearchMapper;
    }

    @Override
    public List<User> searchUsers(Integer currentUserId, String keyword) {
        return userSearchMapper.searchUsersExcludeFriends(currentUserId, keyword);
    }

    @Override
    @Transactional
    public FriendRequest sendRequest(Integer senderId, Integer receiverId, String message) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        if (friendMapper.countFriend(senderId, receiverId) > 0) {
            throw new RuntimeException("已是好友，无需添加");
        }

        int pendingCount = friendRequestMapper.countPendingRequest(senderId, receiverId);
        if (pendingCount > 0) {
            throw new RuntimeException("已发送过好友请求，请等待对方处理");
        }

        int friendCount = friendMapper.countByUserId(senderId);
        if (friendCount >= MAX_FRIENDS) {
            throw new RuntimeException("好友数量已达上限（" + MAX_FRIENDS + "）");
        }

        FriendRequest request = new FriendRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setMessage(message);
        request.setStatus(0);
        request.setCreateTime(new Date());
        request.setUpdateTime(new Date());
        friendRequestMapper.insert(request);
        return request;
    }

    @Override
    public List<FriendRequest> getReceivedRequests(Integer receiverId) {
        return friendRequestMapper.selectReceivedWithSender(receiverId, 0);
    }

    @Override
    public List<FriendRequest> getSentRequests(Integer senderId) {
        return friendRequestMapper.selectSentWithReceiver(senderId);
    }

    @Override
    @Transactional
    public void acceptRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权处理该请求");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        request.setStatus(1);
        request.setUpdateTime(new Date());
        friendRequestMapper.updateById(request);

        Friend friend1 = new Friend();
        friend1.setUserId(userId);
        friend1.setFriendId(request.getSenderId());
        friend1.setCreateTime(new Date());
        friendMapper.insert(friend1);

        Friend friend2 = new Friend();
        friend2.setUserId(request.getSenderId());
        friend2.setFriendId(userId);
        friend2.setCreateTime(new Date());
        friendMapper.insert(friend2);
    }

    @Override
    @Transactional
    public void rejectRequest(Integer requestId, Integer userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权处理该请求");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        request.setStatus(2);
        request.setUpdateTime(new Date());
        friendRequestMapper.updateById(request);
    }

    @Override
    public List<Friend> getFriendList(Integer userId) {
        return friendMapper.selectFriendListWithInfo(userId);
    }

    @Override
    @Transactional
    public void deleteFriend(Integer userId, Integer friendId) {
        friendMapper.deleteBidirectional(userId, friendId);
    }
}