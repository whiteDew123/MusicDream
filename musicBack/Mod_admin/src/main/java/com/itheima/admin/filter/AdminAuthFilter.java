package com.itheima.admin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 管理员接口权限过滤器
 *
 * <p>网关已经完成 JWT 鉴权并透传 X-User-Id / X-Role。
 * 这里只做角色校验：/admin/** 仅允许管理员（role=0）访问。</p>
 */
@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = request.getHeader("X-Role");
        if (!"0".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            String body = "{\"code\": 403, \"message\": \"无管理员权限，禁止访问\", \"data\": null}";
            response.getWriter().write(body);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
