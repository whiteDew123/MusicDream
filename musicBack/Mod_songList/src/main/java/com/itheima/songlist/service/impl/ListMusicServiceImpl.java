package com.itheima.songlist.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.songlist.entity.ListMusic;
import com.itheima.songlist.mapper.ListMusicMapper;
import com.itheima.songlist.service.ListMusicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListMusicServiceImpl extends ServiceImpl<ListMusicMapper, ListMusic> implements ListMusicService {

    @Override
    public List<ListMusic> getSongsByListId(Integer listId) {
        return baseMapper.selectSongsByListId(listId);
    }
}
