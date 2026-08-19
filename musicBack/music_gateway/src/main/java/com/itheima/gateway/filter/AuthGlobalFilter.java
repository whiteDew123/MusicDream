package com.itheima.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局 JWT 鉴权过滤器
 * <p>
 * 白名单路径直接放行，其余路径校验 Authorization 头中的 JWT。
 * 校验通过后将 userId、username、role 写入请求头透传给下游服务。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    /** 白名单路径（无需登录即可访问） */
    private static final List<String> WHITELIST = List.of(
            "/api/login/**",
            "/api/register/**",
            "/api/email/**",
            "/api/music/list",
            "/api/music/search",
            "/api/music/detail/**",
            "/api/singer/list",
            "/api/singer/detail/**",
            "/api/songList/public/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取 token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少认证信息，请先登录");
        }

        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 将用户信息写入请求头，透传给下游服务
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username != null ? username : "")
                    .header("X-Role", role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange, "token 无效或已过期，请重新登录");
        }
    }

    @Override
    public int getOrder() {
        // 优先级，值越小优先级越高
        return 0;
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"code": 401, "message": "%s", "data": null}""".formatted(message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
