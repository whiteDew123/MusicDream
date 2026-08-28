package com.itheima.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 音乐 Mapper
 */
@Mapper
public interface MusicMapper extends BaseMapper<Music> {

    /**
     * 原子递增播放量 +1
     */
    @Update("UPDATE music SET listen_numb = listen_numb + 1 WHERE music_id = #{musicId}")
    int incrementListenNumb(@Param("musicId") Integer musicId);
}
