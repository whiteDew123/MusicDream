package com.itheima.msg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.msg.entity.Msg;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MsgMapper extends BaseMapper<Msg> {

    @Select("SELECT * FROM msg WHERE user_id = #{userId} ORDER BY create_time DESC, id DESC")
    List<Msg> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM msg WHERE user_id = #{userId} AND isread = 0")
    int countUnread(@Param("userId") Integer userId);

    @Update("UPDATE msg SET isread = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE msg SET isread = 1 WHERE user_id = #{userId} AND isread = 0")
    int markAllAsRead(@Param("userId") Integer userId);

    /**
     * 广播批量插入：给每个 userId 各写一条相同标题/内容的消息
     */
    @Insert("<script>" +
            "INSERT INTO msg (title, user_id, msg, create_time, isread) VALUES " +
            "<foreach collection='userIds' item='uid' separator=','>" +
            "(#{title}, #{uid}, #{msg}, #{createTime}, #{isread})" +
            "</foreach>" +
            "</script>")
    int batchInsertForUsers(@Param("title") String title,
                            @Param("msg") String msg,
                            @Param("createTime") LocalDate createTime,
                            @Param("isread") int isread,
                            @Param("userIds") List<Integer> userIds);
}