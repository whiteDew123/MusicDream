package com.itheima.login.controller;

import com.itheima.domain.common.Result;
import com.itheima.domain.entity.User;
import com.itheima.login.dto.LoginRequest;
import com.itheima.login.dto.LoginResponse;
import com.itheima.login.service.UserService;
import com.itheima.login.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 登录接口
 * <p>
 * 网关路由：/api/login/** → StripPrefix=1 → /login/**
 * <p>
 * 注意：网关 AuthGlobalFilter 将 /api/login/** 整体纳入白名单（登录/注册需免鉴权），
 * 因此 /current 与 /update 接口不会被网关解析 JWT 并透传 X-User-Id 头。
 * 解决办法：这两个接口自行从 Authorization 头解析 JWT 获取 userId。
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户登录（POST /login）
     */
    @PostMapping
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前登录用户完整信息（GET /login/current）
     * <p>
     * 自行解析 Authorization 头中的 JWT 拿 userId，兼容网关白名单透传 X-User-Id 的场景。
     */
    @GetMapping("/current")
    public Result<User> current(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestHeader(value = "Authorization", required = false) String auth) {

        Integer resolvedUserId = userId != null ? userId : jwtUtil.parseUserIdFromAuthHeader(auth);
        if (resolvedUserId == null) {
            return Result.fail(401, "未登录或登录已过期");
        }
        User user = userService.getById(resolvedUserId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户资料（PUT /login/update）
     * <p>
     * 允许修改：email / phone / about / imageUrl。
     */
    @PutMapping("/update")
    public Result<String> update(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody User updateInfo) {

        Integer resolvedUserId = userId != null ? userId : jwtUtil.parseUserIdFromAuthHeader(auth);
        if (resolvedUserId == null) {
            return Result.fail(401, "未登录或登录已过期");
        }
        updateInfo.setId(resolvedUserId);
        userService.updateUser(updateInfo);
        return Result.success("更新成功", null);
    }
}
