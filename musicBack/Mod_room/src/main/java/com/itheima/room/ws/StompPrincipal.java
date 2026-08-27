package com.itheima.room.ws;

import java.security.Principal;

/**
 * STOMP 连接的 Principal 简单实现，name 为 userId 的字符串
 */
public class StompPrincipal implements Principal {

    private final Long userId;

    public StompPrincipal(Long userId) {
        this.userId = userId;
    }

    /** 以 userId 字符串作为 Principal 名 */
    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }
}
