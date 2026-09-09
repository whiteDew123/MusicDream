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
     * 获取指定用户信息
     */
    UserVO getUserInfo(Integer userId);

    /**
     * 设置用户信息
     */
    UserVO updateUserInfo(Integer userId, UserUpdateDTO dto);

    /**
     * 修改用户密码
     */
    void updatePassword(Integer userId, PasswordDTO dto);

    /**
     * 修改用户头像
     */
    UserVO updateAvatar(Integer userId, AvatarDTO dto);
}
