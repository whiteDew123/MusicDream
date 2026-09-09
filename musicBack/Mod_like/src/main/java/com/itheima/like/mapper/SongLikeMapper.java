package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.SongLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

/**
 * 歌曲点赞 Mapper
 * <p>
 * - 点赞状态查询使用 @Select
 * - music.likes_count 原子自增/自减使用 @Update，避免行锁冲突
 * - 复合主键 (music_id, user_id) 不使用 BaseMapper.insert/deleteById，改用显式 SQL
 */
@Mapper
public interface SongLikeMapper extends BaseMapper<SongLike> {

    /**
     * 统计某首歌的点赞数（走 song_like 表 COUNT，用于校验冗余列一致性）
     */
    @Select("SELECT COUNT(*) FROM song_like WHERE music_id = #{musicId}")
    int countByMusicId(@Param("musicId") Integer musicId);

    /**
     * music.likes_count 原子 +1（点赞时调用）
     */
    @Update("UPDATE music SET likes_count = likes_count + 1 WHERE music_id = #{musicId}")
    int incrLikeCount(@Param("musicId") Integer musicId);

    /**
     * music.likes_count 原子 -1（取消点赞时调用，带 GREATEST 防止负数）
     */
    @Update("UPDATE music SET likes_count = GREATEST(likes_count - 1, 0) WHERE music_id = #{musicId}")
    int decrLikeCount(@Param("musicId") Integer musicId);

    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT COUNT(*) FROM song_like WHERE music_id = #{musicId} AND user_id = #{userId}")
    int countByMusicAndUser(@Param("musicId") Integer musicId, @Param("userId") Integer userId);

    /**
     * 新增点赞记录（复合主键，显式 SQL）
     */
    @Insert("INSERT INTO song_like (music_id, user_id, create_time) VALUES (#{musicId}, #{userId}, NOW())")
    int insertLike(@Param("musicId") Integer musicId, @Param("userId") Integer userId);

    /**
     * 删除点赞记录（复合主键，显式 SQL）
     */
    @Delete("DELETE FROM song_like WHERE music_id = #{musicId} AND user_id = #{userId}")
    int deleteLike(@Param("musicId") Integer musicId, @Param("userId") Integer userId);
}
