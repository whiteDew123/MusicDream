package com.itheima.friend.ws;

import java.security.Principal;

/**
 * STOMP 连接的 Principal 实现，name 为 userId 的字符串
 */
public class StompPrincipal implements Principal {

    private final Integer userId;

    public StompPrincipal(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Integer getUserId() {
        return userId;
    }
}
