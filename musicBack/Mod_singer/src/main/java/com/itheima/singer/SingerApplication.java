package com.itheima.singer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 歌手模块微服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.singer.mapper")
public class SingerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingerApplication.class, args);
    }
}