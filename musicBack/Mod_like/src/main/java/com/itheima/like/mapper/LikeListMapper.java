package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.LikeList;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏歌单 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 * 复合主键 (userId, listid) 不使用 BaseMapper.save/remove，改用显式 SQL。
 */
@Mapper
public interface LikeListMapper extends BaseMapper<LikeList> {

    /**
     * 查询用户收藏的歌单（联表 song_list 获取歌单名称/封面，联表 user 获取创建者名称）
     */
    @Select("SELECT ll.userId AS user_id, ll.listid AS list_id, " +
            "sl.name AS list_name, sl.image AS list_pic, sl.tags AS list_style, u.username " +
            "FROM likelist ll " +
            "LEFT JOIN song_list sl ON ll.listid = sl.id " +
            "LEFT JOIN user u ON sl.user = u.id " +
            "WHERE ll.userId = #{userId} " +
            "ORDER BY ll.listid DESC")
    List<LikeList> selectLikedListByUserId(@Param("userId") Integer userId);

    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) FROM likelist WHERE userId = #{userId} AND listid = #{listId}")
    int countByUserAndList(@Param("userId") Integer userId, @Param("listId") Integer listId);

    /**
     * 新增收藏（显式 SQL，复合主键）
     */
    @Insert("INSERT INTO likelist (userId, listid) VALUES (#{userId}, #{listId})")
    int insertLike(@Param("userId") Integer userId, @Param("listId") Integer listId);

    /**
     * 移除收藏（显式 SQL，复合主键）
     */
    @Delete("DELETE FROM likelist WHERE userId = #{userId} AND listid = #{listId}")
    int deleteLike(@Param("userId") Integer userId, @Param("listId") Integer listId);
}