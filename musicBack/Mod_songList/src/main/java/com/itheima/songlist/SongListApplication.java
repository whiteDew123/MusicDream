package com.itheima.songlist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 歌单微服务启动类
 * <p>
 * 网关路由：/api/songList/** → StripPrefix=1 → /songList/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.songlist.mapper")
public class SongListApplication {

    public static void main(String[] args) {
        SpringApplication.run(SongListApplication.class, args);
    }
}
