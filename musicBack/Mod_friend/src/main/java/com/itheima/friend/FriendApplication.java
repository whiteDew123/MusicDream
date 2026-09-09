package com.itheima.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 好友服务 微服务启动类
 * <p>
 * 网关路由：/api/friend/** → StripPrefix=1 → /friend/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.friend.mapper")
public class FriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendApplication.class, args);
    }
}