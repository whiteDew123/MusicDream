package com.itheima.friend.service;

import com.itheima.friend.entity.FriendMessage;

import java.util.List;

/**
 * 好友消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     * <p>
     * 事务内完成：① 写 friend_message ② 更新发送方会话最后消息 ③ 更新接收方会话最后消息+未读数+1
     *
     * @param senderId   发送者
     * @param receiverId 接收者
     * @param content    消息内容
     * @param msgType    消息类型: 0=文本 1=图片 2=音频 3=歌曲分享
     */
    FriendMessage send(Integer senderId, Integer receiverId, String content, Integer msgType);

    /**
     * 拉取历史消息（游标分页）
     *
     * @param userId       当前用户
     * @param friendId     对方
     * @param before       游标消息ID（null 表示第一页，拉最新的）
     * @param size         每页数量
     */
    List<FriendMessage> getHistory(Integer userId, Integer friendId, Long before, Integer size);

    /**
     * 标记某会话中对方发给我的消息为已读（同时清零会话未读数）
     */
    void markRead(Integer userId, Integer friendId);

    /**
     * 获取我所有未读消息数（用于顶部 Badge）
     */
    int countAllUnread(Integer userId);
}
