package com.itheima.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.friend.entity.FriendConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 好友会话 Mapper
 * <p>
 * 会话表本质是物化视图：写入消息时多一次更新换取读取时一查即出。
 */
@Mapper
public interface FriendConversationMapper extends BaseMapper<FriendConversation> {

    /**
     * 查询会话列表（带好友信息）
     * <p>
     * 用于左栏好友列表渲染：显示最后消息预览 + 未读红点。
     *
     * @param userId 当前用户ID
     */
    @Select("SELECT fc.*, u.username AS friend_name, u.image_url AS friend_avatar " +
            "FROM friend_conversation fc " +
            "LEFT JOIN user u ON fc.friend_id = u.id " +
            "WHERE fc.user_id = #{userId} " +
            "ORDER BY fc.last_msg_time DESC")
    List<FriendConversation> selectConversationList(@Param("userId") Integer userId);

    /**
     * 查询某条会话（用于发送消息时查找/创建会话）
     */
    @Select("SELECT * FROM friend_conversation " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    FriendConversation selectByUserAndFriend(@Param("userId") Integer userId,
                                              @Param("friendId") Integer friendId);

    /**
     * 更新会话最后消息（发送方视角，已读数不增）
     */
    @Update("UPDATE friend_conversation " +
            "SET last_message = #{lastMessage}, last_msg_time = NOW() " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int updateLastMessage(@Param("userId") Integer userId,
                          @Param("friendId") Integer friendId,
                          @Param("lastMessage") String lastMessage);

    /**
     * 更新会话最后消息并自增未读数（接收方视角，收到对方发来的消息）
     */
    @Update("UPDATE friend_conversation " +
            "SET last_message = #{lastMessage}, last_msg_time = NOW(), unread_count = unread_count + 1 " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int updateLastMessageAndUnread(@Param("userId") Integer userId,
                                   @Param("friendId") Integer friendId,
                                   @Param("lastMessage") String lastMessage);

    /**
     * 清零未读数（接收方点开聊天页时调用）
     */
    @Update("UPDATE friend_conversation SET unread_count = 0 " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int resetUnread(@Param("userId") Integer userId,
                    @Param("friendId") Integer friendId);
}
