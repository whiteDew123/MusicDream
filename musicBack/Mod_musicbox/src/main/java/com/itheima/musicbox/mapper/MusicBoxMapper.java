package com.itheima.musicbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.musicbox.entity.MusicBox;
import com.itheima.musicbox.vo.MusicBoxPlazaVO;
import com.itheima.musicbox.vo.MusicBoxVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MusicBoxMapper extends BaseMapper<MusicBox> {

    /**
     * 查询盲盒广场列表（按时间排序）
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM music_box mb " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "WHERE mb.status = 0 " +
            "ORDER BY mb.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<MusicBoxPlazaVO> selectPlazaList(@Param("userId") Integer userId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    /**
     * 按标签筛选盲盒广场
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM music_box mb " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "WHERE mb.status = 0 AND mb.mood_tag = #{tag} " +
            "ORDER BY mb.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<MusicBoxPlazaVO> selectPlazaListByTag(@Param("userId") Integer userId,
                                                @Param("tag") String tag,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    /**
     * 热门排行（按点赞数排序）
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM music_box mb " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "WHERE mb.status = 0 " +
            "ORDER BY mb.like_count DESC, mb.open_count DESC " +
            "LIMIT #{limit}")
    List<MusicBoxPlazaVO> selectHotBoxes(@Param("userId") Integer userId,
                                         @Param("limit") int limit);

    /**
     * 随机推荐盲盒
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM music_box mb " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "WHERE mb.status = 0 " +
            "ORDER BY RAND() " +
            "LIMIT #{limit}")
    List<MusicBoxPlazaVO> selectRandomBoxes(@Param("userId") Integer userId,
                                            @Param("limit") int limit);

    /**
     * 查询盲盒详情（包含歌曲列表）
     */
    @Select("SELECT mb.*, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked, " +
            "CASE WHEN mbor.id IS NOT NULL THEN 1 ELSE 0 END AS is_opened " +
            "FROM music_box mb " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "LEFT JOIN music_box_open_record mbor ON mb.id = mbor.box_id AND mbor.user_id = #{userId} " +
            "WHERE mb.id = #{boxId} AND mb.status = 0")
    MusicBoxVO selectBoxDetail(@Param("boxId") Integer boxId,
                               @Param("userId") Integer userId);

    /**
     * 查询盲盒歌曲列表（关联歌曲信息和歌手名）
     */
    @Select("SELECT mbs.song_id, m.music_name AS song_name, u.username AS singer_name, m.image_url AS cover_url, mbs.sort_order " +
            "FROM music_box_song mbs " +
            "LEFT JOIN music m ON mbs.song_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "WHERE mbs.box_id = #{boxId} " +
            "ORDER BY mbs.sort_order ASC")
    List<com.itheima.musicbox.vo.MusicBoxSongVO> selectBoxSongs(@Param("boxId") Integer boxId);

    /**
     * 查询我创建的盲盒列表
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "1 AS is_liked " +
            "FROM music_box mb " +
            "WHERE mb.user_id = #{userId} AND mb.status = 0 " +
            "ORDER BY mb.create_time DESC")
    List<MusicBoxPlazaVO> selectMyBoxes(@Param("userId") Integer userId);

    /**
     * 查询我开启过的盲盒列表
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "CASE WHEN mbl.id IS NOT NULL THEN 1 ELSE 0 END AS is_liked " +
            "FROM music_box_open_record mbor " +
            "JOIN music_box mb ON mbor.box_id = mb.id " +
            "LEFT JOIN music_box_like mbl ON mb.id = mbl.box_id AND mbl.user_id = #{userId} " +
            "WHERE mbor.user_id = #{userId} AND mb.status = 0 " +
            "ORDER BY mbor.create_time DESC")
    List<MusicBoxPlazaVO> selectOpenedBoxes(@Param("userId") Integer userId);

    /**
     * 查询我点赞过的盲盒列表
     */
    @Select("SELECT mb.id, mb.title, mb.mood_tag, mb.cover_url, mb.open_count, mb.like_count, mb.create_time, " +
            "(SELECT COUNT(*) FROM music_box_song mbs WHERE mbs.box_id = mb.id) AS song_count, " +
            "1 AS is_liked " +
            "FROM music_box_like mbl " +
            "JOIN music_box mb ON mbl.box_id = mb.id " +
            "WHERE mbl.user_id = #{userId} AND mb.status = 0 " +
            "ORDER BY mbl.create_time DESC")
    List<MusicBoxPlazaVO> selectLikedBoxes(@Param("userId") Integer userId);
}