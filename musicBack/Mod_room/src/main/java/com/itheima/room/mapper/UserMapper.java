package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户查询 Mapper（复用 domain 的 User 实体，用于取房主/成员昵称）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
