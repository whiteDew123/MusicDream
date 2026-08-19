package com.itheima.songlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.songlist.entity.ListMusic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌单-歌曲关联 Mapper 接口
 */
@Mapper
public interface ListMusicMapper extends BaseMapper<ListMusic> {
}
