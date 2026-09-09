package com.itheima.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.msg.entity.SongComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 歌曲评论 Mapper
 * <p>
 * - 列表查询联表 user 获取昵称/头像
 * - music.comment_count 原子自增/自减
 */
@Mapper
public interface SongCommentMapper extends BaseMapper<SongComment> {

    /**
     * 分页查询某首歌的一级评论（parent_id IS NULL），联表 user 取昵称/头像
     */
    @Select("""
            SELECT c.*, u.username, u.image_url AS avatar
            FROM song_comment c
            LEFT JOIN `user` u ON c.user_id = u.id
            WHERE c.music_id = #{musicId} AND c.parent_id IS NULL
            ORDER BY c.is_top DESC, c.create_time DESC, c.id DESC
            """)
    IPage<SongComment> selectTopLevelPage(IPage<SongComment> page, @Param("musicId") Integer musicId);

    /**
     * 查询某条一级评论的楼中楼回复（按时间正序）
     */
    @Select("""
            SELECT c.*, u.username, u.image_url AS avatar
            FROM song_comment c
            LEFT JOIN `user` u ON c.user_id = u.id
            WHERE c.parent_id = #{parentId}
            ORDER BY c.create_time ASC, c.id ASC
            """)
    List<SongComment> selectReplies(@Param("parentId") Long parentId);

    /**
     * 统计某首歌的评论数（走 song_comment 表 COUNT）
     */
    @Select("SELECT COUNT(*) FROM song_comment WHERE music_id = #{musicId}")
    int countByMusicId(@Param("musicId") Integer musicId);

    /**
     * music.comment_count 原子 +1
     */
    @Update("UPDATE music SET comment_count = comment_count + 1 WHERE music_id = #{musicId}")
    int incrCommentCount(@Param("musicId") Integer musicId);

    /**
     * music.comment_count 原子 -1（带 GREATEST 防止负数）
     */
    @Update("UPDATE music SET comment_count = GREATEST(comment_count - 1, 0) WHERE music_id = #{musicId}")
    int decrCommentCount(@Param("musicId") Integer musicId);

    /**
     * 评论点赞数 +1
     */
    @Update("UPDATE song_comment SET likes = likes + 1 WHERE id = #{id}")
    int incrCommentLikes(@Param("id") Long id);
}
