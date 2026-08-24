package com.itheima.admin.service;

import com.itheima.domain.common.Result;

public interface MusicService {

    Result searchMusic(Integer pn, Integer size, String keyword);

    Result freezeMusic(Integer id);

    Result unFreezeMusic(Integer id);

    Result pagePendingMusic(Integer pn, Integer size);

    Result approveMusic(Integer id);

    Result rejectMusic(Integer id, String remark);

    Result deleteMusic(Integer id);
}