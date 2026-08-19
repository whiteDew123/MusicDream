package com.itheima.songlist.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.songlist.entity.LikeList;
import com.itheima.songlist.mapper.LikeListMapper;
import com.itheima.songlist.service.LikeListService;
import org.springframework.stereotype.Service;

@Service
public class LikeListServiceImpl extends ServiceImpl<LikeListMapper, LikeList> implements LikeListService {
}
