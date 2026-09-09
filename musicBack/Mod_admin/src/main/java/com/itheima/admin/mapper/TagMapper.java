package com.itheima.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.Tag;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签字典 Mapper
 */
public interface TagMapper extends BaseMapper<Tag> {

    /** 统计各标签使用频次（含未使用标签），按次数降序 */
    @Select("SELECT t.tag_id, t.code, t.name, t.status, t.create_time, COUNT(mt.id) AS usage_count " +
            "FROM tag t LEFT JOIN music_tag mt ON mt.tag_id = t.tag_id " +
            "GROUP BY t.tag_id, t.code, t.name, t.status, t.create_time " +
            "ORDER BY usage_count DESC, t.tag_id")
    List<Tag> selectWithUsageCount();
}