package com.itheima.capsule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 音乐时空胶囊微服务启动类
 * <p>
 * 网关路由：/api/capsule/** → StripPrefix=1 → /capsule/**
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.itheima.capsule.mapper")
public class CapsuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapsuleApplication.class, args);
    }
}
