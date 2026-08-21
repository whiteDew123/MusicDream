package com.itheima.setting.controller;

import com.itheima.domain.common.Result;
import com.itheima.setting.dto.AvatarDTO;
import com.itheima.setting.dto.PasswordDTO;
import com.itheima.setting.dto.UserUpdateDTO;
import com.itheima.setting.service.SettingService;
import com.itheima.setting.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息设置接口（对接前端 /api/setting/**）
 * <p>
 * 网关 AuthGlobalFilter 会将 JWT 中的 userId 写入请求头 X-User-Id，
 * 本控制器通过 HttpServletRequest 获取当前登录用户 ID。
 */
@RestController
@RequestMapping("/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    /**
     * 获取当前登录用户信息
     * GET /setting/userInfo
     */
    @GetMapping("/userInfo")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        UserVO vo = settingService.getUserInfo(userId);
        if (vo == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success("查询成功", vo);
    }

    /**
     * 更新当前登录用户信息
     * PUT /setting/userInfo
     */
    @PutMapping("/userInfo")
    public Result<UserVO> updateUserInfo(HttpServletRequest request,
                                         @RequestBody UserUpdateDTO dto) {
        Integer userId = getCurrentUserId(request);
        UserVO updated = settingService.updateUserInfo(userId, dto);
        return updated == null ? Result.fail(404, "用户不存在") : Result.success("修改成功", updated);
    }

    /**
     * 修改当前登录用户密码
     * PUT /setting/password
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request,
                                       @RequestBody PasswordDTO dto) {
        Integer userId = getCurrentUserId(request);
        settingService.updatePassword(userId, dto);
        return Result.<Void>success("密码修改成功", null);
    }

    /**
     * 修改当前登录用户头像
     * POST /setting/avatar
     */
    @PostMapping("/avatar")
    public Result<UserVO> updateAvatar(HttpServletRequest request,
                                       @RequestBody AvatarDTO dto) {
        Integer userId = getCurrentUserId(request);
        UserVO updated = settingService.updateAvatar(userId, dto);
        return updated == null ? Result.fail(404, "用户不存在") : Result.success("头像修改成功", updated);
    }

    /**
     * 从请求头获取当前登录用户 ID
     * 网关 AuthGlobalFilter 透传：X-User-Id
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header != null && !header.isBlank()) {
            try {
                return Integer.parseInt(header);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("未登录或用户信息缺失");
    }
}