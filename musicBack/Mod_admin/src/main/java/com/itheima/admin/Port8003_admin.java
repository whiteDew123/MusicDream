package com.itheima.admin;

import com.itheima.domain.config.MybatisPlusPageConfig;
import com.itheima.domain.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.admin.mapper")
@Import(value = {WebMvcConfig.class, MybatisPlusPageConfig.class})
public class Port8003_admin {

    public static void main(String[] args) {
        SpringApplication.run(Port8003_admin.class, args);
    }
}
