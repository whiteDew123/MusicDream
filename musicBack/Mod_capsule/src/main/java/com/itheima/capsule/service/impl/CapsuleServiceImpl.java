package com.itheima.capsule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.capsule.dto.CapsuleDTO;
import com.itheima.capsule.entity.CapsuleLike;
import com.itheima.capsule.mapper.CapsuleLikeMapper;
import com.itheima.capsule.mapper.CapsuleMapper;
import com.itheima.capsule.service.CapsuleService;
import com.itheima.capsule.vo.CapsuleVO;
import com.itheima.domain.entity.Capsule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 胶囊业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapsuleServiceImpl implements CapsuleService {

    private final CapsuleMapper capsuleMapper;
    private final CapsuleLikeMapper capsuleLikeMapper;

    @Override
    public CapsuleVO createCapsule(Integer senderId, CapsuleDTO dto) {
        if (dto.getMusicId() == null) {
            throw new IllegalArgumentException("请选择一首歌曲");
        }
        if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("留言内容不能为空");
        }
        if (dto.getMessage().length() > 500) {
            throw new IllegalArgumentException("留言内容不能超过 500 字");
        }
        if (dto.getUnlockTime() == null) {
            throw new IllegalArgumentException("请选择解锁时间");
        }

        LocalDateTime unlockTime = LocalDateTime.parse(dto.getUnlockTime());
        if (unlockTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("解锁时间必须在未来");
        }

        Capsule capsule = new Capsule();
        capsule.setSenderId(senderId);
        capsule.setReceiverId(dto.getReceiverId() == null ? 0 : dto.getReceiverId());
        capsule.setMusicId(dto.getMusicId());
        capsule.setMessage(dto.getMessage());
        capsule.setUnlockTime(unlockTime);
        capsule.setStatus(0);
        capsule.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()) ? 1 : 0);

        capsuleMapper.insert(capsule);
        log.info("用户 {} 创建胶囊 {}", senderId, capsule.getId());

        return capsuleMapper.selectCapsuleDetail(capsule.getId(), senderId);
    }

    @Override
    public List<CapsuleVO> getMyCapsules(Integer userId) {
        List<CapsuleVO> list = capsuleMapper.selectMyCapsules(userId);
        list.forEach(vo -> {
            if (vo.getStatus() == 0) {
                vo.setMessage(null);
            }
            applyCountdown(vo);
        });
        return list;
    }

    @Override
    public List<CapsuleVO> getReceivedCapsules(Integer userId) {
        List<CapsuleVO> list = capsuleMapper.selectReceivedCapsules(userId);
        list.forEach(vo -> {
            if (vo.getStatus() == 0) {
                vo.setMessage(null);
            }
            applyCountdown(vo);
        });
        return list;
    }

    @Override
    public CapsuleVO getCapsuleDetail(Integer id, Integer userId) {
        CapsuleVO vo = capsuleMapper.selectCapsuleDetail(id, userId);
        if (vo == null) {
            return null;
        }

        // 权限检查：发送者、接收者、或已公开
        boolean canView = vo.getSenderId().equals(userId)
                || (vo.getReceiverId().equals(userId))
                || (vo.getIsPublic() == 1 && vo.getStatus() >= 1);
        if (!canView) {
            throw new IllegalStateException("无权查看此胶囊");
        }

        // 封印状态下隐藏留言
        if (vo.getStatus() == 0) {
            vo.setMessage(null);
        }

        applyCountdown(vo);
        return vo;
    }

    @Override
    public List<CapsuleVO> getPlazaList(Integer userId, int size) {
        int limit = Math.min(Math.max(size, 1), 50);
        List<CapsuleVO> list = capsuleMapper.selectPlazaList(userId, limit);
        list.forEach(vo -> {
            if (vo.getStatus() == 0) {
                vo.setMessage(null);
            }
        });
        return list;
    }

    @Override
    public boolean toggleLike(Integer capsuleId, Integer userId) {
        // 检查是否已点赞
        CapsuleLike existing = capsuleLikeMapper.selectOne(
                new LambdaQueryWrapper<CapsuleLike>()
                        .eq(CapsuleLike::getCapsuleId, capsuleId)
                        .eq(CapsuleLike::getUserId, userId));

        if (existing != null) {
            capsuleLikeMapper.deleteById(existing.getId());
            return false; // 取消点赞
        } else {
            CapsuleLike like = new CapsuleLike();
            like.setCapsuleId(capsuleId);
            like.setUserId(userId);
            capsuleLikeMapper.insert(like);
            return true; // 点赞成功
        }
    }

    @Override
    public boolean makePublic(Integer capsuleId, Integer userId) {
        Capsule capsule = capsuleMapper.selectById(capsuleId);
        if (capsule == null) {
            throw new IllegalArgumentException("胶囊不存在");
        }
        if (!capsule.getSenderId().equals(userId)) {
            throw new IllegalStateException("无权操作他人胶囊");
        }
        if (capsule.getStatus() == 0) {
            throw new IllegalStateException("封印中的胶囊不能公开");
        }

        capsule.setIsPublic(1);
        if (capsule.getStatus() == 1) {
            capsule.setStatus(2);
        }
        capsuleMapper.updateById(capsule);
        return true;
    }

    @Override
    public boolean deleteCapsule(Integer capsuleId, Integer userId) {
        Capsule capsule = capsuleMapper.selectById(capsuleId);
        if (capsule == null) {
            throw new IllegalArgumentException("胶囊不存在");
        }
        if (!capsule.getSenderId().equals(userId)) {
            throw new IllegalStateException("无权删除他人胶囊");
        }

        // 删除点赞记录
        capsuleLikeMapper.delete(
                new LambdaQueryWrapper<CapsuleLike>()
                        .eq(CapsuleLike::getCapsuleId, capsuleId));

        capsuleMapper.deleteById(capsuleId);
        return true;
    }

    /**
     * 计算倒计时描述
     */
    private void applyCountdown(CapsuleVO vo) {
        if (vo.getStatus() != 0) {
            return;
        }
        Duration d = Duration.between(LocalDateTime.now(), vo.getUnlockTime());
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;

        if (days > 0) {
            vo.setCountdown(days + "天" + hours + "小时后解锁");
        } else if (hours > 0) {
            vo.setCountdown(hours + "小时" + minutes + "分钟后解锁");
        } else {
            vo.setCountdown(minutes + "分钟后解锁");
        }
    }
}
