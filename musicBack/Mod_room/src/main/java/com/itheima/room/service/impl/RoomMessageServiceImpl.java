package com.itheima.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.domain.entity.User;
import com.itheima.room.entity.RoomMessage;
import com.itheima.room.mapper.RoomMessageMapper;
import com.itheima.room.mapper.UserMapper;
import com.itheima.room.service.RoomMessageService;
import com.itheima.room.vo.RoomMessageVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 房间消息 Service 实现
 */
@Service
public class RoomMessageServiceImpl extends ServiceImpl<RoomMessageMapper, RoomMessage>
        implements RoomMessageService {

    private final UserMapper userMapper;

    public RoomMessageServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<RoomMessageVO> getMessages(Long roomId, Integer afterSeq) {
        LambdaQueryWrapper<RoomMessage> wrapper = new LambdaQueryWrapper<RoomMessage>()
                .eq(RoomMessage::getRoomId, roomId);
        if (afterSeq != null) {
            wrapper.gt(RoomMessage::getSeq, afterSeq);
        }
        wrapper.orderByAsc(RoomMessage::getSeq);
        List<RoomMessage> list = list(wrapper);

        List<Long> userIds = list.stream().map(RoomMessage::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, User> userMap = loadUserMap(userIds);

        List<RoomMessageVO> result = new ArrayList<>();
        for (RoomMessage m : list) {
            result.add(toVO(m, userMap));
        }
        return result;
    }

    @Override
    public RoomMessageVO send(Long roomId, Long userId, Integer type, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        RoomMessage msg = new RoomMessage();
        msg.setRoomId(roomId);
        msg.setUserId(userId);
        msg.setType(type != null ? type : 0);
        msg.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        msg.setSeq(nextSeq(roomId));
        msg.setCreateTime(LocalDateTime.now());
        save(msg);

        Map<Integer, User> userMap = loadUserMap(userId == null ? Collections.emptyList() : List.of(userId));
        return toVO(msg, userMap);
    }

    /** 计算房间内下一个 seq（递增，用于去重 / 重连补发） */
    private int nextSeq(Long roomId) {
        RoomMessage last = getOne(new LambdaQueryWrapper<RoomMessage>()
                .eq(RoomMessage::getRoomId, roomId)
                .orderByDesc(RoomMessage::getSeq)
                .last("LIMIT 1"));
        return (last == null || last.getSeq() == null) ? 1 : last.getSeq() + 1;
    }

    private RoomMessageVO toVO(RoomMessage m, Map<Integer, User> userMap) {
        RoomMessageVO vo = new RoomMessageVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setType(m.getType());
        vo.setContent(m.getContent());
        vo.setSeq(m.getSeq());
        vo.setCreateTime(m.getCreateTime());
        if (m.getUserId() != null) {
            User u = userMap.get(m.getUserId().intValue());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setImageUrl(u.getImageUrl());
            }
        }
        return vo;
    }

    private Map<Integer, User> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds.stream().map(Long::intValue).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }
}
