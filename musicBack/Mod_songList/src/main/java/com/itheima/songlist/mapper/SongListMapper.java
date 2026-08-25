package com.itheima.songlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.songlist.entity.SongList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 歌单 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 */
@Mapper
public interface SongListMapper extends BaseMapper<SongList> {

    /**
     * 查询公开歌单列表（联表 user 获取创建者名称，联表 likelist 获取当前用户收藏状态）
     *
     * @param userId 当前用户ID，为 null 时不判断收藏状态
     */
    @Select("SELECT s.id, s.name, s.user_id AS user_id, s.pic AS pic, " +
            "s.introduction AS introduction, s.create_date, s.style AS style, " +
            "u.username, " +
            "CASE WHEN l.user_id IS NOT NULL THEN 1 ELSE NULL END AS is_like " +
            "FROM song_list s " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "LEFT JOIN likelist l ON l.list_id = s.id AND l.user_id = #{userId} " +
            "ORDER BY s.create_date DESC")
    List<SongList> selectPublicList(@Param("userId") Integer userId);

    /**
     * 查询歌单详情（联表 user 获取创建者名称，联表 likelist 获取当前用户收藏状态）
     *
     * @param id     歌单ID
     * @param userId 当前用户ID，为 null 时不判断收藏状态
     */
    @Select("SELECT s.id, s.name, s.user_id AS user_id, s.pic AS pic, " +
            "s.introduction AS introduction, s.create_date, s.style AS style, " +
            "u.username, " +
            "CASE WHEN l.user_id IS NOT NULL THEN 1 ELSE NULL END AS is_like " +
            "FROM song_list s " +
            "LEFT JOIN user u ON s.user_id = u.id " +
            "LEFT JOIN likelist l ON l.list_id = s.id AND l.user_id = #{userId} " +
            "WHERE s.id = #{id}")
    SongList selectPublicDetail(@Param("id") Integer id, @Param("userId") Integer userId);
}