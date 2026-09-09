package com.itheima.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.MusicTag;

/**
 * 歌曲-标签关联 Mapper（管理员硬删除歌曲时清理关联数据）
 */
public interface MusicTagMapper extends BaseMapper<MusicTag> {
}