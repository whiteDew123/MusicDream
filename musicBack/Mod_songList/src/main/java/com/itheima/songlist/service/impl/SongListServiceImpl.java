package com.itheima.songlist.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.songlist.entity.SongList;
import com.itheima.songlist.mapper.SongListMapper;
import com.itheima.songlist.service.SongListService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongListServiceImpl extends ServiceImpl<SongListMapper, SongList> implements SongListService {

    @Override
    public List<SongList> getPublicList(Integer userId) {
        return baseMapper.selectPublicList(userId);
    }

    @Override
    public SongList getPublicDetail(Integer id, Integer userId) {
        return baseMapper.selectPublicDetail(id, userId);
    }
}
