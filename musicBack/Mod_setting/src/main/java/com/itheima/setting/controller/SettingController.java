package com.itheima.setting.controller;

import com.itheima.domain.common.Result;
import com.itheima.setting.dto.AvatarDTO;
import com.itheima.setting.dto.PasswordDTO;
import com.itheima.setting.dto.UserUpdateDTO;
import com.itheima.setting.service.SettingService;
import com.itheima.setting.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息设置接口
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    /**
     * 设置用户信息
     */
    @PutMapping("/{userId}")
    public Result<UserVO> updateUserInfo(@PathVariable Integer userId,
                                         @RequestBody UserUpdateDTO dto) {
        UserVO updated = settingService.updateUserInfo(userId, dto);
        return updated == null ? Result.fail(404, "用户不存在") : Result.success("修改成功", updated);
    }

    /**
     * 修改用户密码
     */
    @PutMapping("/{userId}/password")
    public Result<Void> updatePassword(@PathVariable Integer userId,
                                       @RequestBody PasswordDTO dto) {
        settingService.updatePassword(userId, dto);
        return Result.<Void>success("密码修改成功", null);
    }

    /**
     * 修改用户头像
     */
    @PostMapping("/{userId}/avatar")
    public Result<UserVO> updateAvatar(@PathVariable Integer userId,
                                       @RequestBody AvatarDTO dto) {
        UserVO updated = settingService.updateAvatar(userId, dto);
        return updated == null ? Result.fail(404, "用户不存在") : Result.success("头像修改成功", updated);
    }
}
