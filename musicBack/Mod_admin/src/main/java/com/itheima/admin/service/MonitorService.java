package com.itheima.admin.service;

import com.itheima.domain.common.Result;

public interface MonitorService {

    Result getMonitorData();

    Result getTopMusic(Integer limit);
}