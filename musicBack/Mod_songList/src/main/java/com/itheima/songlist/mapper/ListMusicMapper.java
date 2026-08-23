package com.itheima.songlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.songlist.entity.ListMusic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 歌单-歌曲关联 Mapper 接口
 * <p>
 * 自定义查询使用 #{} 预编译参数，防止 SQL 注入。
 */
@Mapper
public interface ListMusicMapper extends BaseMapper<ListMusic> {

    /**
     * 查询歌单内的歌曲列表（联表 music 获取歌曲详情，联表 user 获取歌手名称）
     * <p>
     * 仅返回正常状态（activation=0）的歌曲
     *
     * @param listId 歌单ID
     */
    @Select("SELECT lm.*, " +
            "m.music_name, m.music_url, m.image_url, m.timelength, " +
            "m.listen_numb, m.from_singer AS singer_id, m.lyric, u.username AS singer_name " +
            "FROM list_music lm " +
            "LEFT JOIN music m ON lm.music_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "WHERE lm.list_id = #{listId} AND (m.activation = 0 OR m.activation IS NULL) " +
            "ORDER BY lm.id")
    List<ListMusic> selectSongsByListId(@Param("listId") Integer listId);
}
