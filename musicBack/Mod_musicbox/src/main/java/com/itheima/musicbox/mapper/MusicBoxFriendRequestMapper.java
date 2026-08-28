package com.itheima.musicbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.musicbox.entity.MusicBoxFriendRequest;
import com.itheima.musicbox.vo.MusicBoxFriendRequestVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MusicBoxFriendRequestMapper extends BaseMapper<MusicBoxFriendRequest> {

    /**
     * 查询收到的盲盒交友请求（带发送者信息）
     */
    @Select("SELECT mbfr.*, mb.title AS box_title, " +
            "u.username AS sender_name, u.image_url AS sender_avatar " +
            "FROM music_box_friend_request mbfr " +
            "LEFT JOIN music_box mb ON mbfr.box_id = mb.id " +
            "LEFT JOIN user u ON mbfr.sender_id = u.id " +
            "WHERE mbfr.receiver_id = #{receiverId} AND mbfr.status = #{status} " +
            "ORDER BY mbfr.create_time DESC")
    List<MusicBoxFriendRequestVO> selectReceivedWithInfo(@Param("receiverId") Integer receiverId,
                                                          @Param("status") Integer status);

    /**
     * 检查是否已有待处理的请求
     */
    @Select("SELECT COUNT(*) FROM music_box_friend_request " +
            "WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND status = 0")
    int countPendingRequest(@Param("senderId") Integer senderId,
                            @Param("receiverId") Integer receiverId);
}