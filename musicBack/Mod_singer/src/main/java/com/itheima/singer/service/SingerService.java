package com.itheima.singer.service;

import com.itheima.domain.common.PageResult;
import com.itheima.singer.dto.MusicDTO;
import com.itheima.singer.vo.MusicVO;
import com.itheima.singer.vo.SingerVO;

/**
 * 歌手模块业务接口
 */
public interface SingerService {

    /**
     * 分页查询歌曲
     *
     * @param singerId 歌手ID，可为空
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<MusicVO> pageSongs(Integer singerId, Integer page, Integer size);

    /**
     * 发布歌曲
     *
     * @param dto 歌曲信息
     * @return 发布后的歌曲
     */
    MusicVO publishSong(MusicDTO dto);

    /**
     * 修改歌曲信息
     *
     * @param musicId 歌曲ID
     * @param dto 歌曲信息
     * @return 修改后的歌曲
     */
    MusicVO updateSong(Integer musicId, MusicDTO dto);

    /**
     * 删除歌曲（硬删除，物理删除记录）
     *
     * @param musicId 歌曲ID
     * @return 是否删除成功
     */
    boolean deleteSong(Integer musicId);

    /**
     * 冻结歌曲（activation = 2）
     *
     * @param musicId 歌曲ID
     * @return 是否冻结成功
     */
    boolean freezeSong(Integer musicId);

    /**
     * 解冻歌曲（activation = 0）
     *
     * @param musicId 歌曲ID
     * @return 是否解冻成功
     */
    boolean unfreezeSong(Integer musicId);

    /**
     * 获取歌手数据
     *
     * @param singerId 歌手ID
     * @return 歌手信息
     */
    SingerVO getSingerInfo(Integer singerId);
}