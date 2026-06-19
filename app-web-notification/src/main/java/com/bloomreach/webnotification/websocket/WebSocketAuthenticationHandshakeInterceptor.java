package com.bloomreach.webnotification.websocket;

import static com.bloomreach.webnotification.websocket.WebSocketSessionAttributes.USER_ID;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Authenticates WebSocket handshakes using the current Spring Security context.
 *
 * <p>When authentication succeeds, the authenticated principal name is stored as
 * {@link WebSocketSessionAttributes#USER_ID} on the WebSocket session attributes.
 */
@Component
public class WebSocketAuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Validates the handshake request and stores the authenticated user id on the session.
     *
     * @param request    HTTP upgrade request
     * @param response   HTTP upgrade response
     * @param wsHandler target WebSocket handler
     * @param attributes session attributes populated for the WebSocket connection
     * @return {@code true} when the request is authenticated; {@code false} otherwise
     */
    @Override
    public boolean beforeHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Map<String, Object> attributes) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        attributes.put(USER_ID, authentication.getName());
        return true;
    }

    /**
     * Invoked after the handshake has completed.
     *
     * @param request    HTTP upgrade request
     * @param response   HTTP upgrade response
     * @param wsHandler  target WebSocket handler
     * @param exception  handshake exception, or {@code null} when the handshake succeeded
     */
    @Override
    public void afterHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Exception exception) {
    }
}
