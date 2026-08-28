package com.itheima.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.friend.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友请求 Mapper
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 查询收到的好友请求（带发送者信息）
     */
    @Select("SELECT fr.*, u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM friend_request fr " +
            "LEFT JOIN user u ON fr.sender_id = u.id " +
            "WHERE fr.receiver_id = #{receiverId} AND fr.status = #{status} " +
            "ORDER BY fr.create_time DESC")
    List<FriendRequest> selectReceivedWithSender(@Param("receiverId") Integer receiverId,
                                                  @Param("status") Integer status);

    /**
     * 查询发送的好友请求（带接收者信息）
     */
    @Select("SELECT fr.*, u.username AS receiver_name, u.image_url AS receiver_avatar " +
            "FROM friend_request fr " +
            "LEFT JOIN user u ON fr.receiver_id = u.id " +
            "WHERE fr.sender_id = #{senderId} " +
            "ORDER BY fr.create_time DESC")
    List<FriendRequest> selectSentWithReceiver(@Param("senderId") Integer senderId);

    /**
     * 检查是否已有待处理的请求
     */
    @Select("SELECT COUNT(*) FROM friend_request " +
            "WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND status = 0")
    int countPendingRequest(@Param("senderId") Integer senderId,
                            @Param("receiverId") Integer receiverId);
}