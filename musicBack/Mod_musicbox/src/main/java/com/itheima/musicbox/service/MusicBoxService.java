package com.itheima.musicbox.service;

import com.itheima.musicbox.dto.MusicBoxCreateDTO;
import com.itheima.musicbox.entity.MusicBoxFriendRequest;
import com.itheima.musicbox.vo.MusicBoxFriendRequestVO;
import com.itheima.musicbox.vo.MusicBoxPlazaVO;
import com.itheima.musicbox.vo.MusicBoxVO;

import java.util.List;

public interface MusicBoxService {

    /**
     * 创建盲盒
     */
    Integer createBox(Integer userId, MusicBoxCreateDTO dto);

    /**
     * 查询盲盒广场列表（最新）
     */
    List<MusicBoxPlazaVO> getPlazaList(Integer userId, int page, int size);

    /**
     * 按标签筛选盲盒广场
     */
    List<MusicBoxPlazaVO> getPlazaListByTag(Integer userId, String tag, int page, int size);

    /**
     * 热门排行
     */
    List<MusicBoxPlazaVO> getHotBoxes(Integer userId, int limit);

    /**
     * 随机推荐
     */
    List<MusicBoxPlazaVO> getRandomBoxes(Integer userId, int limit);

    /**
     * 获取盲盒详情（不记录开启次数）
     */
    MusicBoxVO getBoxDetail(Integer boxId, Integer userId);

    /**
     * 开启盲盒
     */
    MusicBoxVO openBox(Integer boxId, Integer userId);

    /**
     * 点赞/取消点赞盲盒
     */
    void toggleLike(Integer boxId, Integer userId);

    /**
     * 查询我创建的盲盒
     */
    List<MusicBoxPlazaVO> getMyBoxes(Integer userId);

    /**
     * 查询我开启过的盲盒
     */
    List<MusicBoxPlazaVO> getOpenedBoxes(Integer userId);

    /**
     * 查询我点赞过的盲盒
     */
    List<MusicBoxPlazaVO> getLikedBoxes(Integer userId);

    /**
     * 删除盲盒（软删除）
     */
    void deleteBox(Integer boxId, Integer userId);

    /**
     * 发送盲盒交友请求
     */
    void sendFriendRequest(Integer boxId, Integer senderId, Integer receiverId, String message);

    /**
     * 查询收到的盲盒交友请求
     */
    List<MusicBoxFriendRequestVO> getReceivedFriendRequests(Integer receiverId);

    /**
     * 接受盲盒交友请求
     */
    void acceptFriendRequest(Integer requestId, Integer userId);

    /**
     * 拒绝盲盒交友请求
     */
    void rejectFriendRequest(Integer requestId, Integer userId);
}