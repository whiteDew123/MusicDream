package com.itheima.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.friend.entity.Friend;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友关系 Mapper
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 查询好友列表（带好友信息）
     */
    @Select("SELECT f.*, u.username AS friend_name, u.image_url AS friend_avatar " +
            "FROM friend f " +
            "LEFT JOIN user u ON f.friend_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<Friend> selectFriendListWithInfo(@Param("userId") Integer userId);

    /**
     * 检查是否已是好友
     */
    @Select("SELECT COUNT(*) FROM friend " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int countFriend(@Param("userId") Integer userId,
                    @Param("friendId") Integer friendId);

    /**
     * 删除双向好友关系
     */
    @Delete("DELETE FROM friend WHERE (user_id = #{userId} AND friend_id = #{friendId}) " +
            "OR (user_id = #{friendId} AND friend_id = #{userId})")
    int deleteBidirectional(@Param("userId") Integer userId,
                            @Param("friendId") Integer friendId);

    /**
     * 统计好友数量
     */
    @Select("SELECT COUNT(*) FROM friend WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
}