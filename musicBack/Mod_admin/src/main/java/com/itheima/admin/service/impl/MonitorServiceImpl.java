package com.itheima.admin.service.impl;

import com.itheima.admin.service.MonitorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘监控数据实现
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    private final JdbcTemplate jdbcTemplate;

    public MonitorServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> getMonitor() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 用户总数：排除管理员，只统计歌手和普通用户
        Integer userTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `user` WHERE role != 0", Integer.class);

        // 音乐总数：全部音乐
        Integer musicTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM music", Integer.class);

        // 歌手总数
        Integer singerTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `user` WHERE role = 1", Integer.class);

        // 歌单总数
        Integer songListTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM song_list", Integer.class);

        // 待审核歌曲数
        Integer pendingMusicTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM music WHERE activation = 3", Integer.class);

        // 今日新增用户
        Integer todayNewUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `user` WHERE create_time = ?", Integer.class, LocalDate.now());

        data.put("userTotal", userTotal == null ? 0 : userTotal);
        data.put("musicTotal", musicTotal == null ? 0 : musicTotal);
        data.put("singerTotal", singerTotal == null ? 0 : singerTotal);
        data.put("songListTotal", songListTotal == null ? 0 : songListTotal);
        data.put("pendingMusicTotal", pendingMusicTotal == null ? 0 : pendingMusicTotal);
        data.put("todayNewUsers", todayNewUsers == null ? 0 : todayNewUsers);

        return data;
    }

    @Override
    public List<Map<String, Object>> getTopMusic(Integer limit) {
        int size = limit == null || limit < 1 ? 5 : Math.min(limit, 20);
        String sql = "SELECT music_id AS musicId, music_name AS musicName, "
                + "from_singer AS fromSinger, listen_numb AS listenNumb, image_url AS imageUrl "
                + "FROM music WHERE activation = 0 ORDER BY listen_numb DESC LIMIT ?";

        return jdbcTemplate.query(sql, ps -> ps.setInt(1, size), (rs, rowNum) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("musicId", rs.getInt("musicId"));
            item.put("musicName", rs.getString("musicName"));
            item.put("fromSinger", rs.getInt("fromSinger"));
            item.put("listenNumb", rs.getInt("listenNumb"));
            item.put("imageUrl", rs.getString("imageUrl"));
            return item;
        });
    }

    @Override
    public List<Map<String, Object>> getTrend(Integer days) {
        int size = days == null || days < 1 ? 7 : Math.min(days, 30);
        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = size - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);

            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `user` WHERE create_time = ?", Integer.class, date);
            Integer musicCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM music WHERE create_time = ?", Integer.class, date);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("userCount", userCount == null ? 0 : userCount);
            item.put("musicCount", musicCount == null ? 0 : musicCount);
            list.add(item);
        }

        return list;
    }
}
