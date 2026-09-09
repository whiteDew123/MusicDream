package com.itheima.singer.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 歌手模块权限过滤器
 *
 * <p>公开路径（推荐歌手、歌手详情等）直接放行；其余路径仅允许
 * 管理员（role=0）和歌手（role=1）访问。
 * 具体歌曲归属校验在 Controller / Service 层完成。</p>
 */
@Component
public class SingerAuthFilter extends OncePerRequestFilter {

    /** 公开路径前缀（不校验角色） */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/singer/recommend/",
            "/singer/detail/",
            "/singer/info/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/singer")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 公开接口直接放行
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String role = request.getHeader("X-Role");
        if (!"0".equals(role) && !"1".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            String body = "{\"code\": 403, \"message\": \"无歌手或管理员权限，禁止访问\", \"data\": null}";
            response.getWriter().write(body);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
