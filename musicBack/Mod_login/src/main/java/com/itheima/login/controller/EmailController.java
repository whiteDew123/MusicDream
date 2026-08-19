package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import com.itheima.login.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码接口
 * <p>
 * 网关路由：/api/email/** → StripPrefix=1 → /email/**
 */
@RestController
@RequestMapping("/email")
public class EmailController {

    private final UserService userService;

    public EmailController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 发送注册验证码（GET /email/code?email=xxx）
     */
    @GetMapping("/code")
    public Result<Void> sendCode(@RequestParam("email") String email) {
        userService.sendEmailCode(email);
        return Result.success("验证码已发送，请查收邮箱", null);
    }
}
