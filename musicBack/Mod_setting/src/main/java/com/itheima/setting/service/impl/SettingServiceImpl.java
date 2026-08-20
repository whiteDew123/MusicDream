package com.itheima.setting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.domain.entity.User;
import com.itheima.setting.dto.AvatarDTO;
import com.itheima.setting.dto.PasswordDTO;
import com.itheima.setting.dto.UserUpdateDTO;
import com.itheima.setting.mapper.UserMapper;
import com.itheima.setting.service.SettingService;
import com.itheima.setting.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户信息设置业务实现
 *
 * <p>使用 MyBatis Plus 条件构造器 + LambdaUpdateWrapper 完成更新操作。</p>
 */
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final UserMapper userMapper;

    @Override
    public UserVO updateUserInfo(Integer userId, UserUpdateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        User user = getUser(userId);
        if (user == null) {
            return null;
        }

        // 校验唯一字段是否被其他用户占用
        checkUnique(user, "username", dto.getUsername());
        checkUnique(user, "email", dto.getEmail());
        checkUnique(user, "phone", dto.getPhone());

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId);

        boolean changed = false;
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.set(User::getUsername, dto.getUsername());
            changed = true;
        }
        if (StringUtils.hasText(dto.getEmail())) {
            wrapper.set(User::getEmail, dto.getEmail());
            changed = true;
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.set(User::getPhone, dto.getPhone());
            changed = true;
        }
        if (dto.getAbout() != null) {
            wrapper.set(User::getAbout, dto.getAbout());
            changed = true;
        }

        // 没有任何字段需要更新时直接返回原用户信息
        if (!changed) {
            return toVO(user);
        }

        userMapper.update(null, wrapper);
        return toVO(userMapper.selectById(userId));
    }

    @Override
    public void updatePassword(Integer userId, PasswordDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getOldPassword()) || !StringUtils.hasText(dto.getNewPassword())) {
            throw new IllegalArgumentException("原密码和新密码不能为空");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }

        User user = getUser(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!md5(dto.getOldPassword()).equals(user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getPassword, md5(dto.getNewPassword()));
        userMapper.update(null, wrapper);
    }

    @Override
    public UserVO updateAvatar(Integer userId, AvatarDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getImageUrl())) {
            throw new IllegalArgumentException("头像地址不能为空");
        }

        User user = getUser(userId);
        if (user == null) {
            return null;
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getImageUrl, dto.getImageUrl());
        userMapper.update(null, wrapper);

        return toVO(userMapper.selectById(userId));
    }

    /**
     * 校验唯一字段是否与其他用户冲突
     */
    private void checkUnique(User current, String field, String newValue) {
        if (!StringUtils.hasText(newValue)) {
            return;
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .ne(User::getId, current.getId());

        boolean conflict = switch (field) {
            case "username" -> {
                if (newValue.equals(current.getUsername())) {
                    yield false;
                }
                wrapper.eq(User::getUsername, newValue);
                yield userMapper.selectCount(wrapper) > 0;
            }
            case "email" -> {
                if (newValue.equals(current.getEmail())) {
                    yield false;
                }
                wrapper.eq(User::getEmail, newValue);
                yield userMapper.selectCount(wrapper) > 0;
            }
            case "phone" -> {
                if (newValue.equals(current.getPhone())) {
                    yield false;
                }
                wrapper.eq(User::getPhone, newValue);
                yield userMapper.selectCount(wrapper) > 0;
            }
            default -> false;
        };

        if (conflict) {
            throw new IllegalArgumentException(field + "已被其他用户使用");
        }
    }

    private User getUser(Integer userId) {
        return userId == null ? null : userMapper.selectById(userId);
    }

    private UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setActivation(user.getActivation());
        vo.setCreateTime(user.getCreateTime());
        vo.setImageUrl(user.getImageUrl());
        vo.setAbout(user.getAbout());
        return vo;
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
