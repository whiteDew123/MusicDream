package com.itheima.setting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户信息设置模块微服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.setting.mapper")
public class SettingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettingApplication.class, args);
    }
}
