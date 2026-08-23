package com.itheima.songlist.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.songlist.entity.ListMusic;

import java.util.List;

/**
 * 歌单-歌曲关联 Service 接口
 */
public interface ListMusicService extends IService<ListMusic> {

    /**
     * 查询歌单内的歌曲列表（联表返回歌曲详情和歌手名称）
     *
     * @param listId 歌单ID
     */
    List<ListMusic> getSongsByListId(Integer listId);
}
