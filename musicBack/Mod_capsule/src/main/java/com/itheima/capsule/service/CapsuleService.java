package com.itheima.capsule.service;

import com.itheima.capsule.dto.CapsuleDTO;
import com.itheima.capsule.vo.CapsuleVO;

import java.util.List;

/**
 * 胶囊业务接口
 */
public interface CapsuleService {

    /**
     * 创建胶囊
     */
    CapsuleVO createCapsule(Integer senderId, CapsuleDTO dto);

    /**
     * 我创建的胶囊
     */
    List<CapsuleVO> getMyCapsules(Integer userId);

    /**
     * 写给我的胶囊
     */
    List<CapsuleVO> getReceivedCapsules(Integer userId);

    /**
     * 胶囊详情（封印状态下隐藏留言）
     */
    CapsuleVO getCapsuleDetail(Integer id, Integer userId);

    /**
     * 时空广场
     */
    List<CapsuleVO> getPlazaList(Integer userId, int size);

    /**
     * 点赞/取消点赞
     */
    boolean toggleLike(Integer capsuleId, Integer userId);

    /**
     * 设为公开
     */
    boolean makePublic(Integer capsuleId, Integer userId);

    /**
     * 删除胶囊
     */
    boolean deleteCapsule(Integer capsuleId, Integer userId);
}
