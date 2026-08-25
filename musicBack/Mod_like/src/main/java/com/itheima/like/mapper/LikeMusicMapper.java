package com.itheima.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.like.entity.LikeMusic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏歌曲 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 * 复合主键 (user, music) 不使用 BaseMapper.save/remove，改用显式 SQL。
 */
@Mapper
public interface LikeMusicMapper extends BaseMapper<LikeMusic> {

    /**
     * 查询用户收藏的歌曲（联表 music 获取歌曲名称/封面）
     */
    @Select("SELECT lm.music AS music_id, lm.user AS user_id, " +
            "m.music_name, m.music_url, m.image_url AS music_pic, " +
            "m.image_url, m.timelength, m.lyric, m.listen_numb, u.username AS singer_name " +
            "FROM mylike lm " +
            "LEFT JOIN music m ON lm.music = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "WHERE lm.user = #{userId} " +
            "ORDER BY m.create_time DESC")
    List<LikeMusic> selectLikedMusicByUserId(@Param("userId") Integer userId);

    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) FROM mylike WHERE user = #{userId} AND music = #{musicId}")
    int countByUserAndMusic(@Param("userId") Integer userId, @Param("musicId") Integer musicId);

    /**
     * 新增收藏（显式 SQL，复合主键）
     */
    @Insert("INSERT INTO mylike (user, music, create_date) VALUES (#{userId}, #{musicId}, NOW())")
    int insertLike(@Param("userId") Integer userId, @Param("musicId") Integer musicId);

    /**
     * 移除收藏（显式 SQL，复合主键）
     */
    @Delete("DELETE FROM mylike WHERE user = #{userId} AND music = #{musicId}")
    int deleteLike(@Param("userId") Integer userId, @Param("musicId") Integer musicId);
}
