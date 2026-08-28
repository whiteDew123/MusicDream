package com.itheima.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.friend.entity.FriendMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 好友消息 Mapper
 * <p>
 * 游标分页策略：用 before={msgId} 代替 OFFSET，避免深翻页性能问题。
 */
@Mapper
public interface FriendMessageMapper extends BaseMapper<FriendMessage> {

    /**
     * 拉取历史消息（游标分页）
     * <p>
     * before 为 null 时拉最新一页；否则拉 id < before 的更早消息。
     *
     * @param conversationId 会话ID
     * @param before        游标消息ID（null 表示第一页）
     * @param size          每页数量
     */
    @Select("<script>" +
            "SELECT m.*, u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM friend_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE m.conversation_id = #{conversationId} " +
            "<if test='before != null'>AND m.id &lt; #{before} </if>" +
            "ORDER BY m.id DESC " +
            "LIMIT #{size}" +
            "</script>")
    List<FriendMessage> selectMessageHistory(@Param("conversationId") Long conversationId,
                                             @Param("before") Long before,
                                             @Param("size") Integer size);

    /**
     * 简单按会话拉最新 N 条（用于 WebSocket 重连后补偿）
     */
    @Select("SELECT m.*, u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM friend_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE m.conversation_id = #{conversationId} " +
            "ORDER BY m.id DESC LIMIT #{size}")
    List<FriendMessage> selectLatestMessages(@Param("conversationId") Long conversationId,
                                             @Param("size") Integer size);

    /**
     * 批量标记某会话中对方发给我的消息为已读
     *
     * @param receiverId 我的用户ID
     * @param senderId   对方用户ID
     */
    @Update("UPDATE friend_message SET is_read = 1 " +
            "WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0")
    int updateReadStatus(@Param("receiverId") Integer receiverId,
                         @Param("senderId") Integer senderId);

    /**
     * 统计某人发给我的未读消息数
     */
    @Select("SELECT COUNT(*) FROM friend_message " +
            "WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0")
    int countUnread(@Param("receiverId") Integer receiverId,
                    @Param("senderId") Integer senderId);

    /**
     * 统计我所有未读消息数（用于顶部 Badge）
     */
    @Select("SELECT COUNT(*) FROM friend_message WHERE receiver_id = #{userId} AND is_read = 0")
    int countAllUnread(@Param("userId") Integer userId);
}
