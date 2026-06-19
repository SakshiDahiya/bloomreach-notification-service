package com.bloomreach.websockets.config;

import com.bloomreach.websockets.websocket.WebSocketSessionHandler;
import com.bloomreach.websockets.websocket.WebSocketAuthenticationHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers native Spring WebSocket handlers for the websockets application.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public static final String WEBSOCKET_SESSIONS_PATH = "/ws/sessions";

    private final WebSocketSessionHandler webSocketSessionHandler;
    private final WebSocketAuthenticationHandshakeInterceptor webSocketAuthenticationHandshakeInterceptor;

    /**
     * @param webSocketSessionHandler handler for WebSocket sessions
     * @param webSocketAuthenticationHandshakeInterceptor interceptor that authenticates handshakes
     */
    public WebSocketConfig(
            final WebSocketSessionHandler webSocketSessionHandler,
            final WebSocketAuthenticationHandshakeInterceptor webSocketAuthenticationHandshakeInterceptor) {
        this.webSocketSessionHandler = webSocketSessionHandler;
        this.webSocketAuthenticationHandshakeInterceptor = webSocketAuthenticationHandshakeInterceptor;
    }

    /**
     * Registers the WebSocket session endpoint and its authentication interceptor.
     *
     * @param registry WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketSessionHandler, WEBSOCKET_SESSIONS_PATH)
                .addInterceptors(webSocketAuthenticationHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
