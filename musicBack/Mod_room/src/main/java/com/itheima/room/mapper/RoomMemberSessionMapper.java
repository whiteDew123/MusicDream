package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.room.entity.RoomMemberSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RoomMemberSessionMapper extends BaseMapper<RoomMemberSession> {

    /** 关闭一个未关闭的 session（正常离开或心跳离线） */
    @Update("UPDATE room_member_session SET leave_time = #{leaveTime}, " +
            "duration_sec = TIMESTAMPDIFF(SECOND, enter_time, #{leaveTime}) " +
            "WHERE room_id = #{roomId} AND user_id = #{userId} AND leave_time IS NULL")
    int closeSession(@Param("roomId") Long roomId,
                     @Param("userId") Long userId,
                     @Param("leaveTime") LocalDateTime leaveTime);

    /** 关闭所有在指定房间 + 指定用户列表中的未关闭 session（批量心跳离线用） */
    @Update("<script>" +
            "UPDATE room_member_session SET leave_time = #{leaveTime}, " +
            "duration_sec = TIMESTAMPDIFF(SECOND, enter_time, #{leaveTime}) " +
            "WHERE room_id = #{roomId} AND leave_time IS NULL AND user_id IN " +
            "<foreach item='uid' collection='userIds' open='(' separator=',' close=')'>#{uid}</foreach>" +
            "</script>")
    int closeSessions(@Param("roomId") Long roomId,
                      @Param("userIds") List<Long> userIds,
                      @Param("leaveTime") LocalDateTime leaveTime);

    /** 查询房间最近 N 条进出记录 */
    @Select("SELECT * FROM room_member_session WHERE room_id = #{roomId} " +
            "ORDER BY enter_time DESC LIMIT #{limit}")
    List<RoomMemberSession> selectRecentByRoom(@Param("roomId") Long roomId,
                                               @Param("limit") int limit);

    /** 查询用户在房间内的累计观看秒数（所有已关闭 session 的 duration_sec + 当前未关闭的计时） */
    @Select("SELECT COALESCE(SUM(CASE WHEN leave_time IS NOT NULL THEN duration_sec " +
            "ELSE TIMESTAMPDIFF(SECOND, enter_time, NOW()) END), 0) " +
            "FROM room_member_session WHERE room_id = #{roomId} AND user_id = #{userId}")
    int sumDurationSec(@Param("roomId") Long roomId,
                       @Param("userId") Long userId);
}
