package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.room.entity.RoomStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoomStatsMapper extends BaseMapper<RoomStats> {

    /** 当在线数超过 peak_online 时更新峰值 */
    @Update("UPDATE room_stats SET peak_online = GREATEST(peak_online, #{onlineCount}) " +
            "WHERE room_id = #{roomId} AND #{onlineCount} > COALESCE(peak_online, 0)")
    int updatePeakIfHigher(@Param("roomId") Long roomId,
                           @Param("onlineCount") int onlineCount);

    /** 全量重算房间累计观看分钟数（从 room_member_session 表汇总） */
    @Update("UPDATE room_stats s SET s.total_watch_minutes = (" +
            "  SELECT IFNULL(SUM(" +
            "    CASE WHEN sess.duration_sec IS NOT NULL " +
            "         THEN FLOOR(sess.duration_sec / 60) " +
            "         ELSE FLOOR(TIMESTAMPDIFF(SECOND, sess.enter_time, NOW()) / 60) " +
            "    END), 0)" +
            "  FROM room_member_session sess" +
            "  WHERE sess.room_id = #{roomId}" +
            ") WHERE s.room_id = #{roomId}")
    int recalcTotalWatchMinutes(@Param("roomId") Long roomId);
}
