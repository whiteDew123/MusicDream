package com.itheima.room;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 一起听·播放室 微服务启动类
 * <p>
 * 网关路由：/api/room/** → StripPrefix=1 → /room/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.room.mapper")
public class RoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomApplication.class, args);
    }
}
