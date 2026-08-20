package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.LikeMusic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏歌曲 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 */
@Mapper
public interface LikeMusicMapper extends BaseMapper<LikeMusic> {

    /**
     * 查询用户收藏的歌曲（联表 music 获取歌曲名称/封面）
     *
     * @param userId 用户ID
     */
    @Select("SELECT lm.*, m.music_name, m.image_url AS music_pic " +
            "FROM like_music lm " +
            "LEFT JOIN music m ON lm.music_id = m.music_id " +
            "WHERE lm.user_id = #{userId} " +
            "ORDER BY lm.create_date DESC")
    List<LikeMusic> selectLikedMusicByUserId(@Param("userId") Integer userId);
}
