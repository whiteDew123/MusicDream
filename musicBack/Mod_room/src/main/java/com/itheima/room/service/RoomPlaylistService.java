package com.itheima.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.room.dto.PlaylistSortDTO;
import com.itheima.room.entity.RoomPlaylist;
import com.itheima.room.vo.RoomPlaylistItemVO;

import java.util.List;

/**
 * 房间歌单 Service 接口
 */
public interface RoomPlaylistService extends IService<RoomPlaylist> {

    /** 查询房间歌单（含歌曲展示信息，按 sort_order 升序） */
    List<RoomPlaylistItemVO> getList(Long roomId, Long userId);

    /** 添加歌曲到歌单（校验重复与歌曲存在性） */
    void add(Long roomId, Long musicId, Long userId);

    /** 从歌单移除歌曲 */
    void remove(Long roomId, Long musicId, Long userId);

    /** 歌单排序 */
    void sort(Long roomId, PlaylistSortDTO dto, Long userId);
}
