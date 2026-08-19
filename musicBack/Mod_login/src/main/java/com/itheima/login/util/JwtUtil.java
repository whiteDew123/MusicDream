package com.itheima.login.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 签发 token 时将 userId 放入 subject，username / role 写入自定义声明，
 * 与网关 AuthGlobalFilter 解析逻辑保持一致。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationHours;

    /**
     * 生成 JWT token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT token
     */
    public String generateToken(Integer userId, String username, Integer role) {
        long now = System.currentTimeMillis();
        long expirationMillis = expirationHours * 3600_000L;
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role == null ? "" : String.valueOf(role))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(key)
                .compact();
    }
}
