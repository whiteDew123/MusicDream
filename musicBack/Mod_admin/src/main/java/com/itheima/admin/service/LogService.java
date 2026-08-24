package com.itheima.admin.service;

import com.itheima.domain.common.Result;

public interface LogService {

    Result searchLog(Integer pn, Integer size, String keyword);

    void saveLog(String userName, String doSome, String musicName);
}