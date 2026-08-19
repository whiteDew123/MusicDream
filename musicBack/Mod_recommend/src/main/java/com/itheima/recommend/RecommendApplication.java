package com.itheima.recommend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 歌曲推荐搜索模块微服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.recommend.mapper")
public class RecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendApplication.class, args);
    }
}