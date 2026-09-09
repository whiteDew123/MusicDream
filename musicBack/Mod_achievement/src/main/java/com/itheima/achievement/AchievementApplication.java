package com.itheima.achievement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 听歌成就微服务启动类
 * <p>
 * 网关路由：/api/achievement/** → StripPrefix=1 → /achievement/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.achievement.mapper")
public class AchievementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AchievementApplication.class, args);
    }
}
