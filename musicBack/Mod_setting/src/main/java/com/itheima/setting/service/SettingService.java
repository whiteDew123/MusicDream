package com.itheima.setting.service;

import com.itheima.setting.dto.AvatarDTO;
import com.itheima.setting.dto.PasswordDTO;
import com.itheima.setting.dto.UserUpdateDTO;
import com.itheima.setting.vo.UserVO;

/**
 * 用户信息设置业务接口
 */
public interface SettingService {

    /**
     * 设置用户信息
     *
     * @param userId 用户ID
     * @param dto 用户信息
     * @return 更新后的用户信息
     */
    UserVO updateUserInfo(Integer userId, UserUpdateDTO dto);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param dto 原密码和新密码
     */
    void updatePassword(Integer userId, PasswordDTO dto);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param dto 头像地址
     * @return 更新后的用户信息
     */
    UserVO updateAvatar(Integer userId, AvatarDTO dto);
}
