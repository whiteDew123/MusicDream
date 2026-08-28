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
     * 拉取历史消息（游标分页，按用户对双向查询）
     * <p>
     * 不再依赖 conversation_id，直接用 (sender_id, receiver_id) 双向匹配。
     * 这样无论消息存的是哪个方向的会话 id，都能正确拉取。
     *
     * @param userId   当前用户ID
     * @param friendId 对方用户ID
     * @param before   游标消息ID（null 表示第一页）
     * @param size     每页数量
     */
    @Select("<script>" +
            "SELECT m.*, u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM friend_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE ((m.sender_id = #{userId} AND m.receiver_id = #{friendId}) " +
            "   OR (m.sender_id = #{friendId} AND m.receiver_id = #{userId})) " +
            "<if test='before != null'>AND m.id &lt; #{before} </if>" +
            "ORDER BY m.id DESC " +
            "LIMIT #{size}" +
            "</script>")
    List<FriendMessage> selectMessageHistory(@Param("userId") Integer userId,
                                             @Param("friendId") Integer friendId,
                                             @Param("before") Long before,
                                             @Param("size") Integer size);

    /**
     * 简单按用户对拉最新 N 条（用于 WebSocket 重连后补偿）
     */
    @Select("SELECT m.*, u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM friend_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE ((m.sender_id = #{userId} AND m.receiver_id = #{friendId}) " +
            "   OR (m.sender_id = #{friendId} AND m.receiver_id = #{userId})) " +
            "ORDER BY m.id DESC LIMIT #{size}")
    List<FriendMessage> selectLatestMessages(@Param("userId") Integer userId,
                                             @Param("friendId") Integer friendId,
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
