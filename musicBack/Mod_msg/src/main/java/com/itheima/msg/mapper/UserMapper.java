package com.itheima.msg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id FROM `user` WHERE activation = 0")
    List<Integer> findAllActiveUserIds();
}