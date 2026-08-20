package com.itheima.admin.service;

import com.itheima.domain.common.Result;

public interface UserService {

    Result searchUser(Integer pn, Integer size, String keyword);

    Result freezeUser(Integer id);

    Result unFreezeUser(Integer id);
}
