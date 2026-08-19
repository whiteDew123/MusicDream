package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import com.itheima.domain.entity.User;
import com.itheima.login.dto.RegisterRequest;
import com.itheima.login.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册接口
 * <p>
 * 网关路由：/api/register/** → StripPrefix=1 → /register/**
 */
@RestController
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册（POST /register）
     * <p>
     * 请求体示例：
     * <pre>{"username": "alice", "password": "123456", "email": "alice@qq.com", "emailCode": "123456"}</pre>
     */
    @PostMapping
    public Result<User> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        // 返回前清除敏感字段
        user.setPassword(null);
        return Result.success("注册成功", user);
    }
}
