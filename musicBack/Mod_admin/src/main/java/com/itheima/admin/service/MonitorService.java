package com.itheima.admin.service;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘监控数据接口
 */
public interface MonitorService {

    /**
     * 获取统计概览数据
     */
    Map<String, Object> getMonitor();

    /**
     * 获取热门歌曲排行
     */
    List<Map<String, Object>> getTopMusic(Integer limit);

    /**
     * 获取最近 N 天新增趋势
     */
    List<Map<String, Object>> getTrend(Integer days);
}
