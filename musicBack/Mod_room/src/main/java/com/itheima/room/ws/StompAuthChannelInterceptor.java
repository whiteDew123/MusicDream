package com.itheima.room.ws;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * STOMP 鉴权通道拦截器
 * <p>
 * 在 CONNECT 帧上校验客户端携带的 token（与网关共享密钥），
 * 校验通过后把 userId 写入 Principal，供后续 @MessageMapping 处理器读取。
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("token");
            if (token != null && !token.isBlank()) {
                try {
                    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
                    Long userId = Long.valueOf(claims.getSubject());
                    accessor.setUser(new StompPrincipal(userId));
                    accessor.setLeaveMutable(true);
                } catch (Exception e) {
                    // token 无效：不注入 Principal，后续处理器会拒绝该用户
                    accessor.setLeaveMutable(true);
                }
            }
        }
        return message;
    }
}
