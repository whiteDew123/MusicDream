package com.itheima.msg.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.msg.entity.SongComment;
import com.itheima.msg.mapper.SongCommentMapper;
import com.itheima.msg.service.SongCommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SongCommentServiceImpl extends ServiceImpl<SongCommentMapper, SongComment> implements SongCommentService {

    @Override
    public Page<SongComment> listComments(Integer musicId, Integer pn, Integer size) {
        Page<SongComment> page = new Page<>(pn, size);
        baseMapper.selectTopLevelPage(page, musicId);
        return page;
    }

    @Override
    public List<SongComment> listReplies(Long parentId) {
        return baseMapper.selectReplies(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SongComment publish(Integer musicId, Integer userId, String content,
                               Long parentId, Integer toUserId) {
        SongComment comment = new SongComment();
        comment.setMusicId(musicId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setToUserId(toUserId);
        comment.setLikes(0);
        comment.setIsTop(0);
        comment.setCreateTime(new Date());
        baseMapper.insert(comment);
        baseMapper.incrCommentCount(musicId);
        // 回填联表字段
        SongComment reloaded = baseMapper.selectById(comment.getId());
        return reloaded;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long commentId, Integer userId, Integer role) {
        SongComment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }
        // 仅作者本人或管理员可删
        boolean canDelete = comment.getUserId().equals(userId) || (role != null && role == 0);
        if (!canDelete) {
            return false;
        }
        int rows = baseMapper.deleteById(commentId);
        if (rows > 0) {
            baseMapper.decrCommentCount(comment.getMusicId());
            return true;
        }
        return false;
    }

    @Override
    public boolean like(Long commentId) {
        int rows = baseMapper.incrCommentLikes(commentId);
        return rows > 0;
    }

    @Override
    public int countByMusicId(Integer musicId) {
        return baseMapper.countByMusicId(musicId);
    }
}
