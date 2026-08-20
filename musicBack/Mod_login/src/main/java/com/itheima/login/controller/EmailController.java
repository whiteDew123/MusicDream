package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import com.itheima.login.service.UserService;
import org.springframework.web.bind.annotation.*;

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
    public Result<Void> sendCodeGet(@RequestParam("email") String email) {
        System.out.println("[EMAIL-GET] 收到的 email: '" + email + "'");
        userService.sendEmailCode(email);
        return Result.success("验证码已发送，请查收邮箱", null);
    }

    /**
     * 发送注册验证码（POST /email/code，body: {"email":"xxx"}）
     * 推荐使用 POST，避免 URL 编码问题
     */
    @PostMapping("/code")
    public Result<Void> sendCodePost(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        System.out.println("[EMAIL-POST] 收到的 email: '" + email + "'");
        userService.sendEmailCode(email);
        return Result.success("验证码已发送，请查收邮箱", null);
    }

    @lombok.Data
    public static class EmailRequest {
        private String email;
    }
}
