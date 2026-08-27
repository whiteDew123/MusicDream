package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.room.entity.RoomMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间成员 Mapper
 */
@Mapper
public interface RoomMemberMapper extends BaseMapper<RoomMember> {
}
