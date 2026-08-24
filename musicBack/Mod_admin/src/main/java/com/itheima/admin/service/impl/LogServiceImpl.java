package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.LogMapper;
import com.itheima.admin.service.LogService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    @Override
    public Result searchLog(Integer pn, Integer size, String keyword) {
        Page<Log> logPage = new Page<>(pn, size);
        QueryWrapper<Log> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(e -> e
                    .like("userName", keyword)
                    .or()
                    .like("do_some", keyword)
                    .or()
                    .like("`MusicName`", keyword)
            );
        }
        Page<Log> page = logMapper.selectPage(logPage, wrapper);
        return Result.success("查询成功", page);
    }

    @Override
    public void saveLog(String userName, String doSome, String musicName) {
        Log log = new Log();
        log.setUserName(userName);
        log.setDoSome(doSome);
        log.setMusicName(musicName);
        log.setCreateDate(LocalDate.now());
        logMapper.insert(log);
    }
}