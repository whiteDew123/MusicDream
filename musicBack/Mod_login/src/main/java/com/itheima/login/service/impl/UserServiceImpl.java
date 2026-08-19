package com.itheima.login.service.impl;

import com.itheima.domain.entity.User;
import com.itheima.login.dto.LoginRequest;
import com.itheima.login.dto.LoginResponse;
import com.itheima.login.dto.RegisterRequest;
import com.itheima.login.mapper.UserMapper;
import com.itheima.login.service.UserService;
import com.itheima.login.util.JwtUtil;
import com.itheima.login.util.Md5Util;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用户业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String EMAIL_CODE_KEY_PREFIX = "email:code:";
    private static final String DEFAULT_AVATAR = "https://cdn.musicdreamer.com/default-avatar.png";

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${mail.host}")
    private String mailHost;

    @Value("${mail.port}")
    private int mailPort;

    @Value("${mail.username}")
    private String mailUsername;

    @Value("${mail.password}")
    private String mailPassword;

    @Value("${mail.from-name}")
    private String fromName;

    @Value("${mail.code-expire-minutes}")
    private long codeExpireMinutes;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }

        // 支持用户名 / 邮箱 / 手机号登录
        User user = userMapper.selectByUsername(account);
        if (user == null && account.contains("@")) {
            user = userMapper.selectByEmail(account);
        }
        if (user == null) {
            user = userMapper.selectByPhone(account);
        }
        if (user == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        // MD5 密码校验
        if (!Md5Util.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 账号锁定状态：1 表示锁定
        if (user.getActivation() != null && user.getActivation() == 1) {
            throw new IllegalStateException("账号已被锁定，请联系管理员");
        }

        // 签发 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getImageUrl()
        );
    }

    @Override
    public User register(RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        String emailCode = request.getEmailCode();

        // 基础参数校验
        if (!StringUtils.hasText(username) || username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3-20 之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("密码长度需在 6-32 之间");
        }
        if (!StringUtils.hasText(email) || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 用户名 / 邮箱唯一性校验
        if (userMapper.selectByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已被占用");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 邮箱验证码校验
        String storedCode = redisTemplate.opsForValue().get(EMAIL_CODE_KEY_PREFIX + email);
        if (!StringUtils.hasText(emailCode) || !emailCode.equals(storedCode)) {
            throw new IllegalArgumentException("邮箱验证码无效或已过期");
        }
        // 验证通过后立即删除，防止重复使用
        redisTemplate.delete(EMAIL_CODE_KEY_PREFIX + email);

        // 构建用户实体并入库，密码 MD5 加密
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(password));
        user.setEmail(email);
        user.setRole(2);           // 普通用户
        user.setActivation(0);     // 正常状态
        user.setCreateTime(LocalDate.now());
        user.setImageUrl(DEFAULT_AVATAR);

        userMapper.insert(user);
        return user;
    }

    @Override
    public void sendEmailCode(String email) {
        if (!StringUtils.hasText(email) || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        // 已注册邮箱不再发验证码（注册场景）
        if (userMapper.selectByEmail(email) != null) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        // 生成 6 位数字验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        // 存入 Redis，设置过期时间
        redisTemplate.opsForValue().set(EMAIL_CODE_KEY_PREFIX + email, code, codeExpireMinutes, TimeUnit.MINUTES);

        // 发送邮件
        sendMail(email, code);
    }

    @Override
    public User getById(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 通过 commons-email 发送验证码邮件
     */
    private void sendMail(String to, String code) {
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(mailHost);
            email.setSmtpPort(mailPort);
            email.setAuthentication(mailUsername, mailPassword);
            email.setSSLOnConnect(true);
            email.setFrom(mailUsername, fromName);
            email.setSubject("【MusicDreamer】邮箱验证码");
            email.setMsg("您正在注册 MusicDreamer 账号，验证码为：" + code
                    + "，有效期 " + codeExpireMinutes + " 分钟。如非本人操作请忽略此邮件。");
            email.addTo(to);
            email.send();
        } catch (Exception e) {
            // 邮件发送失败不抛出，验证码已存入 Redis 便于开发期联调
            // 生产环境建议改用异步队列 + 重试机制
            System.err.println("[Mod_login] 邮件发送失败 -> " + to + "，验证码：" + code + "，原因：" + e.getMessage());
        }
    }
}
