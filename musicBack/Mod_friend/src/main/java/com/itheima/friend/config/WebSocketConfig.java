package com.itheima.friend.config;

import com.itheima.friend.ws.StompAuthChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Mod_friend WebSocket / STOMP 配置
 * <p>
 * - 端点：/ws-friend（独立于 Mod_room 的 /ws，互不冲突）
 * - 应用前缀：/app（客户端 → 服务端）
 * - 用户广播：/user/{userId}/queue（点对点推送，每个用户只收自己的消息）
 * - 鉴权：CONNECT 帧携带 token，由 StompAuthChannelInterceptor 校验并注入 Principal
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfig(StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Bean
    public TaskScheduler stompHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("friend-stomp-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-friend")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用 user broker 前缀 /user，用于点对点推送（每个用户订阅 /user/{userId}/queue/...）
        registry.enableSimpleBroker("/user")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(stompHeartbeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
