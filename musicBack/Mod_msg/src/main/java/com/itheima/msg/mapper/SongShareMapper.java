package com.itheima.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.msg.entity.SongShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 歌曲分享 Mapper
 * <p>
 * - 分享记录写入 song_share
 * - music.share_count 原子自增（同一用户同渠道每日去重由 Service 层保证）
 */
@Mapper
public interface SongShareMapper extends BaseMapper<SongShare> {

    /**
     * music.share_count 原子 +1
     */
    @Update("UPDATE music SET share_count = share_count + 1 WHERE music_id = #{musicId}")
    int incrShareCount(@Param("musicId") Integer musicId);

    /**
     * 从 music 表读取分享数（冗余列）
     */
    @Select("SELECT share_count FROM music WHERE music_id = #{musicId}")
    int selectShareCount(@Param("musicId") Integer musicId);
}
