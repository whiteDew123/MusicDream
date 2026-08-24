package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.LikeList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏歌单 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 */
@Mapper
public interface LikeListMapper extends BaseMapper<LikeList> {

    /**
     * 查询用户收藏的歌单（联表 song_list 获取歌单名称/封面，联表 user 获取创建者名称）
     *
     * @param userId 用户ID
     */
    @Select("SELECT ll.*, sl.name AS list_name, sl.image AS list_pic, sl.tags AS list_style, u.username " +
            "FROM likelist ll " +
            "LEFT JOIN song_list sl ON ll.listid = sl.id " +
            "LEFT JOIN user u ON sl.user = u.id " +
            "WHERE ll.userId = #{userId} " +
            "ORDER BY ll.userId DESC")
    List<LikeList> selectLikedListByUserId(@Param("userId") Integer userId);
}