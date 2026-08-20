package com.itheima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.admin.mapper.MusicMapper;
import com.itheima.admin.mapper.UserMapper;
import com.itheima.admin.service.UserService;
import com.itheima.domain.common.Result;
import com.itheima.domain.entity.Music;
import com.itheima.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MusicMapper musicMapper;

    @Override
    public Result searchUser(Integer pn, Integer size, String keyword) {
        Page<User> userPage = new Page<>(pn, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(e -> e
                    .like("username", keyword)
                    .or()
                    .like("id", keyword)
                    .or()
                    .like("phone", keyword)
                    .or()
                    .like("email", keyword)
            ).and(e -> e.ne("role", 0));
        } else {
            wrapper.ne("role", 0);
        }
        Page<User> page = userMapper.selectPage(userPage, wrapper);
        return Result.success("查询成功", page);
    }

    @Override
    public Result freezeUser(Integer id) {
        UpdateWrapper<User> userWrapper = new UpdateWrapper<>();
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        userWrapper.eq("id", id).setSql("activation = 1");
        userMapper.update(null, userWrapper);
        musicWrapper.eq("from_singer", id).setSql("activation = 2");
        musicMapper.update(null, musicWrapper);
        return Result.success("冻结成功", null);
    }

    @Override
    public Result unFreezeUser(Integer id) {
        UpdateWrapper<User> userWrapper = new UpdateWrapper<>();
        UpdateWrapper<Music> musicWrapper = new UpdateWrapper<>();
        userWrapper.eq("id", id).setSql("activation = 0");
        userMapper.update(null, userWrapper);
        musicWrapper.eq("from_singer", id).setSql("activation = 0");
        musicMapper.update(null, musicWrapper);
        return Result.success("解冻成功", null);
    }
}
