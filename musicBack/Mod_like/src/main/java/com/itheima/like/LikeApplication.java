package com.itheima.like;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 收藏微服务启动类
 * <p>
 * 网关路由：/api/like/** → StripPrefix=1 → /like/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.like.mapper")
public class LikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LikeApplication.class, args);
    }
}
