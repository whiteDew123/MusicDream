package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.admin.mapper.LogMapper;
import com.itheima.admin.mapper.MusicMapper;
import com.itheima.admin.mapper.UserMapper;
import com.itheima.admin.service.MonitorService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Log;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private LogMapper logMapper;

    @Override
    public Result getMonitorData() {
        Map<String, Object> data = new HashMap<>();

        Long userCount = userMapper.selectCount(
                new QueryWrapper<User>().ne("role", 0)
        );
        data.put("userCount", userCount);

        Long musicCount = musicMapper.selectCount(
                new QueryWrapper<Music>().eq("activation", 0)
        );
        data.put("musicCount", musicCount);

        Long singerCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("role", 1)
        );
        data.put("singerCount", singerCount);

        Long todayLogCount = logMapper.selectCount(
                new QueryWrapper<Log>().eq("create_date", LocalDate.now())
        );
        data.put("todayLogCount", todayLogCount);

        return Result.success("查询成功", data);
    }

    @Override
    public Result getTopMusic(Integer limit) {
        QueryWrapper<Music> wrapper = new QueryWrapper<>();
        wrapper.eq("activation", 0)
                .orderByDesc("listen_numb")
                .last("LIMIT " + limit);
        List<Music> musicList = musicMapper.selectList(wrapper);
        return Result.success("查询成功", musicList);
    }
}