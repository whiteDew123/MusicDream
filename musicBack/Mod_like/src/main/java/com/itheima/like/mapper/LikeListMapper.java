package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.LikeList;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LikeListMapper extends BaseMapper<LikeList> {

    @Select("SELECT ll.user_id, ll.list_id, " +
            "sl.name AS list_name, sl.pic AS list_pic, sl.style AS list_style, u.username " +
            "FROM likelist ll " +
            "LEFT JOIN song_list sl ON ll.list_id = sl.id " +
            "LEFT JOIN user u ON sl.user_id = u.id " +
            "WHERE ll.user_id = #{userId} " +
            "ORDER BY ll.list_id DESC")
    List<LikeList> selectLikedListByUserId(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM likelist WHERE user_id = #{userId} AND list_id = #{listId}")
    int countByUserAndList(@Param("userId") Integer userId, @Param("listId") Integer listId);

    @Insert("INSERT INTO likelist (user_id, list_id) VALUES (#{userId}, #{listId})")
    int insertLike(@Param("userId") Integer userId, @Param("listId") Integer listId);

    @Delete("DELETE FROM likelist WHERE user_id = #{userId} AND list_id = #{listId}")
    int deleteLike(@Param("userId") Integer userId, @Param("listId") Integer listId);
}