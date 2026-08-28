package com.itheima.login.util;

import io.jsonwebtoken.Claims;
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

    /**
     * 从 token 中解析 userId。
     * <p>
     * 网关 AuthGlobalFilter 将 /api/login/** 整体纳入白名单，
     * 导致 /login/current 与 /login/update 不会被网关解析 JWT 并透传 X-User-Id 头，
     * 因此这两个接口需要自行从 Authorization 头解析身份。
     *
     * @param token 原始 JWT token（不含 Bearer 前缀）
     * @return 用户ID；解析失败或过期返回 null
     */
    public Integer parseUserId(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Integer.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Authorization 头（形如 "Bearer xxx"）中解析 userId。
     */
    public Integer parseUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return parseUserId(authHeader.substring(7));
    }
}
