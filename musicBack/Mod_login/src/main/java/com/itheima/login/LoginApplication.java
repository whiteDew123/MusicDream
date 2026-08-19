package com.itheima.login;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户登录微服务启动类
 * <p>
 * 端口 8001，对外提供登录、注册、邮箱验证码等服务，注册到 Nacos 后由网关路由 lb://mod-login 转发。
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itheima.login.mapper")
public class LoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginApplication.class, args);
    }
}
