package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import com.itheima.login.dto.LoginRequest;
import com.itheima.login.dto.LoginResponse;
import com.itheima.login.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口
 * <p>
 * 网关路由：/api/login/** → StripPrefix=1 → /login/**
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录（POST /login）
     * <p>
     * 请求体示例：
     * <pre>{"account": "alice", "password": "123456"}</pre>
     */
    @PostMapping
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
}
