package com.itheima.login.service;

import com.itheima.domain.entity.User;
import com.itheima.login.dto.LoginRequest;
import com.itheima.login.dto.LoginResponse;
import com.itheima.login.dto.RegisterRequest;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * 用户登录：支持用户名 / 邮箱 / 手机号三种方式，密码使用 MD5 校验。
     *
     * @param request 登录请求
     * @return 登录成功后的 token 与用户基本信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册：校验邮箱验证码、用户名 / 邮箱唯一性后，将密码 MD5 加密入库。
     *
     * @param request 注册请求
     * @return 新注册用户实体
     */
    User register(RegisterRequest request);

    /**
     * 发送邮箱验证码：生成 6 位验证码存入 Redis 并通过邮件发送。
     *
     * @param email 收件邮箱
     */
    void sendEmailCode(String email);

    /**
     * 根据 ID 查询用户（密码字段置空）。
     */
    User getById(Integer userId);
}
