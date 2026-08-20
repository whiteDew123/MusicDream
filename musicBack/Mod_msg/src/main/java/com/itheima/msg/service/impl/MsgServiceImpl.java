package com.itheima.msg.service.impl;

import com.itheima.msg.dto.MsgVO;
import com.itheima.msg.dto.PublishMsgRequest;
import com.itheima.msg.entity.Msg;
import com.itheima.msg.mapper.MsgMapper;
import com.itheima.msg.service.MsgService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MsgServiceImpl implements MsgService {

    private static final String UNREAD_COUNT_KEY_PREFIX = "msg:unread:";

    private final MsgMapper msgMapper;
    private final StringRedisTemplate redisTemplate;

    public MsgServiceImpl(MsgMapper msgMapper, StringRedisTemplate redisTemplate) {
        this.msgMapper = msgMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Msg publish(PublishMsgRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("消息标题不能为空");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("接收用户ID不能为空");
        }
        if (!StringUtils.hasText(request.getMsg())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        Msg msg = new Msg();
        msg.setTitle(request.getTitle());
        msg.setUserId(request.getUserId());
        msg.setMsg(request.getMsg());
        msg.setCreateTime(LocalDate.now());
        msg.setIsread(1);

        msgMapper.insert(msg);

        // 同步 Redis 未读数 +1
        String key = UNREAD_COUNT_KEY_PREFIX + request.getUserId();
        redisTemplate.opsForValue().increment(key);

        return msg;
    }

    @Override
    public List<MsgVO> getMyMessages(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<Msg> msgs = msgMapper.selectByUserId(userId);
        return msgs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int getUnreadCount(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        String key = UNREAD_COUNT_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return Integer.parseInt(cached);
        }

        // Redis 无缓存，查库并回填缓存
        int count = msgMapper.countUnread(userId);
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    @Override
    public void markAsRead(Integer id, Integer userId) {
        if (id == null) {
            throw new IllegalArgumentException("消息ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        int updated = msgMapper.markAsRead(id, userId);
        if (updated == 0) {
            throw new IllegalArgumentException("消息不存在或无权操作");
        }

        // 同步 Redis 未读数 -1（负数时修正为 0）
        String key = UNREAD_COUNT_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            int current = Integer.parseInt(cached);
            int newVal = Math.max(0, current - 1);
            redisTemplate.opsForValue().set(key, String.valueOf(newVal));
        }
    }

    @Override
    public void markAllAsRead(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        msgMapper.markAllAsRead(userId);

        // 清空缓存，下次查询时重新从 DB 回填
        String key = UNREAD_COUNT_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    private MsgVO convertToVO(Msg msg) {
        MsgVO vo = new MsgVO();
        vo.setId(msg.getId());
        vo.setTitle(msg.getTitle());
        vo.setMsg(msg.getMsg());
        vo.setCreateTime(msg.getCreateTime());
        vo.setIsread(msg.getIsread());
        return vo;
    }
}