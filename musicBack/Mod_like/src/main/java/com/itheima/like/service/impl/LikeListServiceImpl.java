package com.itheima.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.like.entity.LikeList;
import com.itheima.like.mapper.LikeListMapper;
import com.itheima.like.service.LikeListService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeListServiceImpl extends ServiceImpl<LikeListMapper, LikeList> implements LikeListService {

    @Override
    public List<LikeList> getLikedList(Integer userId) {
        return baseMapper.selectLikedListByUserId(userId);
    }
}
