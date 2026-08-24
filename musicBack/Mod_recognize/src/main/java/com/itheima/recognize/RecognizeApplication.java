package com.itheima.recognize;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 听歌识曲微服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.recognize.mapper")
public class RecognizeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecognizeApplication.class, args);
    }
}
