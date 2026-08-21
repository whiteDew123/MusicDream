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
     * 分页查询歌曲（支持关键词和状态筛选）
     *
     * @param singerId 歌手ID，可为空
     * @param page 页码
     * @param size 每页条数
     * @param keyword 搜索关键词，可为空
     * @param activation 状态筛选，可为空
     * @return 分页结果
     */
    PageResult<MusicVO> pageSongs(Integer singerId, Integer page, Integer size, String keyword, Integer activation);

    /**
     * 分页查询歌曲（支持关键词、状态和审核状态筛选）
     *
     * @param singerId 歌手ID，可为空
     * @param page 页码
     * @param size 每页条数
     * @param keyword 搜索关键词，可为空
     * @param activation 状态筛选，可为空
     * @param auditStatus 审核状态筛选，可为空
     * @return 分页结果
     */
    PageResult<MusicVO> pageSongs(Integer singerId, Integer page, Integer size, String keyword, Integer activation, Integer auditStatus);

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
     * 删除歌曲（软删除：将 activation 置为 1，用户锁定/下架）
     *
     * @param musicId 歌曲ID
     * @return 是否删除成功
     */
    boolean deleteSong(Integer musicId);

    /**
     * 发布歌曲（管理端 addMusic 接口）
     *
     * @param dto 歌曲信息
     * @return 发布后的歌曲
     */
    MusicVO addMusic(MusicDTO dto);

    /**
     * 更新歌曲状态（管理员锁定/解锁）
     *
     * @param musicId 歌曲ID
     * @param activation 状态: 0-正常, 1-锁定
     * @return 更新后的歌曲
     */
    MusicVO updateMusicStatus(Integer musicId, Integer activation);

    /**
     * 获取歌手数据
     *
     * @param singerId 歌手ID
     * @return 歌手信息
     */
    SingerVO getSingerInfo(Integer singerId);

    /**
     * 审核歌曲（通过/驳回）
     *
     * @param musicId 歌曲ID
     * @param auditStatus 审核状态: 1-通过, 2-驳回
     * @param auditRemark 审核备注（驳回原因）
     * @return 审核后的歌曲
     */
    MusicVO auditSong(Integer musicId, Integer auditStatus, String auditRemark);
}
