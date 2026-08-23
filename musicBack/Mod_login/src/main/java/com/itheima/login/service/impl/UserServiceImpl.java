package com.itheima.login.service.impl;

import com.itheima.domain.entity.User;
import com.itheima.login.dto.LoginRequest;
import com.itheima.login.dto.LoginResponse;
import com.itheima.login.dto.RegisterRequest;
import com.itheima.login.mapper.UserMapper;
import com.itheima.login.service.UserService;
import com.itheima.login.util.JwtUtil;
import com.itheima.login.util.Md5Util;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        if (!Md5Util.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        if (user.getActivation() != null && user.getActivation() == 1) {
            throw new IllegalStateException("账号已被锁定，请联系管理员");
        }

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

        if (!StringUtils.hasText(username) || username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3-20 之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("密码长度需在 6-32 之间");
        }
        if (!StringUtils.hasText(email) || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        if (userMapper.selectByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已被占用");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        String storedCode = null;
        try {
            storedCode = redisTemplate.opsForValue().get(EMAIL_CODE_KEY_PREFIX + email);
        } catch (Exception e) {
            log.warn("Redis 连接失败，无法验证邮箱验证码: {}", e.getMessage());
        }

        if (!StringUtils.hasText(emailCode) || !emailCode.equals(storedCode)) {
            throw new IllegalArgumentException("邮箱验证码无效或已过期");
        }

        try {
            redisTemplate.delete(EMAIL_CODE_KEY_PREFIX + email);
        } catch (Exception e) {
            log.warn("Redis 连接失败，无法删除验证码: {}", e.getMessage());
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(password));
        user.setEmail(email);
        user.setRole(2);
        user.setActivation(0);
        user.setCreateTime(LocalDate.now());
        user.setImageUrl(DEFAULT_AVATAR);

        userMapper.insert(user);
        return user;
    }

    @Override
    public void sendEmailCode(String email) {
        log.info("sendEmailCode 收到: '{}'", email);
        if (!StringUtils.hasText(email) || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));

        try {
            redisTemplate.opsForValue().set(EMAIL_CODE_KEY_PREFIX + email, code, codeExpireMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 连接失败，验证码将不会被缓存: {}", e.getMessage());
        }

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
            log.warn("邮件发送失败 -> {}，验证码：{}，原因：{}", to, code, e.getMessage());
        }
    }
}
