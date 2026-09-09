package com.itheima.musicbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.musicbox.mapper")
public class MusicBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicBoxApplication.class, args);
    }
}