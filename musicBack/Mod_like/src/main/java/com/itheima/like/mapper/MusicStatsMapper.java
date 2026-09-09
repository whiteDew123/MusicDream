package com.itheima.like.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 音乐统计 Mapper
 * <p>
 * 从 music 表读取冗余列（likes_count / comment_count / share_count），
 * 避免每次都 COUNT 统计表，性能优先。
 */
@Mapper
public interface MusicStatsMapper {

    /**
     * 一次查询获取歌曲的全部交互统计数
     *
     * @param musicId 歌曲ID
     * @return {"likesCount": N, "commentCount": N, "shareCount": N}
     */
    @Select("SELECT likes_count AS likesCount, " +
            "comment_count AS commentCount, " +
            "share_count AS shareCount " +
            "FROM music WHERE music_id = #{musicId}")
    Map<String, Object> selectStatsByMusicId(@Param("musicId") Integer musicId);
}