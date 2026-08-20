package com.itheima.songlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.songlist.entity.LikeList;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏歌单 Mapper 接口
 */
@Mapper
public interface LikeListMapper extends BaseMapper<LikeList> {
}
