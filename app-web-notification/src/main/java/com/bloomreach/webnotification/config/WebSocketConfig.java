package com.bloomreach.webnotification.config;

import com.bloomreach.webnotification.websocket.NotificationWebSocketHandler;
import com.bloomreach.webnotification.websocket.WebSocketAuthenticationHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers native Spring WebSocket handlers for the web notification application.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String NOTIFICATIONS_WEBSOCKET_PATH = "/ws/notifications";

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final WebSocketAuthenticationHandshakeInterceptor webSocketAuthenticationHandshakeInterceptor;

    /**
     * @param notificationWebSocketHandler handler for notification WebSocket sessions
     * @param webSocketAuthenticationHandshakeInterceptor interceptor that authenticates handshakes
     */
    public WebSocketConfig(
            final NotificationWebSocketHandler notificationWebSocketHandler,
            final WebSocketAuthenticationHandshakeInterceptor webSocketAuthenticationHandshakeInterceptor) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.webSocketAuthenticationHandshakeInterceptor = webSocketAuthenticationHandshakeInterceptor;
    }

    /**
     * Registers the notification WebSocket endpoint and its authentication interceptor.
     *
     * @param registry WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, NOTIFICATIONS_WEBSOCKET_PATH)
                .addInterceptors(webSocketAuthenticationHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
