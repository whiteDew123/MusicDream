package com.itheima.songlist.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.songlist.entity.SongList;

import java.util.List;

/**
 * 歌单 Service 接口
 */
public interface SongListService extends IService<SongList> {

    /**
     * 查询公开歌单列表（带创建者名称和当前用户收藏状态）
     */
    List<SongList> getPublicList(Integer userId);

    /**
     * 查询歌单详情（带创建者名称和当前用户收藏状态）
     */
    SongList getPublicDetail(Integer id, Integer userId);
}
