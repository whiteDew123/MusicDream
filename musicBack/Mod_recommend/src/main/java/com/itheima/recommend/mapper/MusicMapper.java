package com.itheima.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.Music;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 音乐 Mapper
 */
public interface MusicMapper extends BaseMapper<Music> {

    /**
     * 批量查询歌曲-标签关联（music_tag 表），供标签画像推荐使用。
     * 仅返回关联关系由 Service 层聚合，禁止 SQL 关联。
     */
    @Select("<script>" +
            "SELECT music_id, tag_id FROM music_tag " +
            "WHERE music_id IN " +
            "<foreach collection='musicIds' item='mid' open='(' separator=',' close=')'>#{mid}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectTagRelations(@Param("musicIds") List<Integer> musicIds);
}
