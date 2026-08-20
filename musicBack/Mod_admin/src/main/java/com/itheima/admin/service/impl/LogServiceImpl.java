package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.LogMapper;
import com.itheima.admin.service.LogService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

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
                    .like("MusicName", keyword)
            );
        }
        wrapper.orderByDesc("create_date");
        Page<Log> page = logMapper.selectPage(logPage, wrapper);
        return Result.success("查询成功", page);
    }
}