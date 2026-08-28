package com.itheima.friend.mapper;

import com.itheima.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户查询 Mapper（用于搜索用户）
 */
@Mapper
public interface UserSearchMapper {

    /**
     * 根据用户名模糊搜索用户，排除已好友
     */
    @Select("SELECT u.id, u.username, u.image_url, u.about " +
            "FROM user u " +
            "WHERE u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "AND u.id != #{currentUserId} " +
            "AND u.id NOT IN (" +
            "  SELECT f.friend_id FROM friend f WHERE f.user_id = #{currentUserId}" +
            ") " +
            "LIMIT 20")
    List<User> searchUsersExcludeFriends(@Param("currentUserId") Integer currentUserId,
                                         @Param("keyword") String keyword);
}