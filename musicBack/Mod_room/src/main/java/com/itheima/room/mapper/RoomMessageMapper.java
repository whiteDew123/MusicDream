package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.room.entity.RoomMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间消息 Mapper
 */
@Mapper
public interface RoomMessageMapper extends BaseMapper<RoomMessage> {
}
