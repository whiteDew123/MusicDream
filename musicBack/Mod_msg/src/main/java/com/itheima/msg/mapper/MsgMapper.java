package com.itheima.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.msg.entity.Msg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MsgMapper extends BaseMapper<Msg> {

    @Select("SELECT * FROM msg WHERE user_id = #{userId} ORDER BY create_time DESC, id DESC")
    List<Msg> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM msg WHERE user_id = #{userId} AND isread = 1")
    int countUnread(@Param("userId") Integer userId);

    @Update("UPDATE msg SET isread = 0 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE msg SET isread = 0 WHERE user_id = #{userId} AND isread = 1")
    int markAllAsRead(@Param("userId") Integer userId);
}