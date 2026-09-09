package com.itheima.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.room.entity.RoomMessage;
import com.itheima.room.vo.RoomMessageVO;

import java.util.List;

/**
 * 房间消息 Service 接口
 */
public interface RoomMessageService extends IService<RoomMessage> {

    /** 查询房间消息（seq 大于 after 的才返回，用于重连补发） */
    List<RoomMessageVO> getMessages(Long roomId, Integer afterSeq);

    /** 发送一条文字 / Emoji 消息（自动分配房间内 seq） */
    RoomMessageVO send(Long roomId, Long userId, Integer type, String content);
}
