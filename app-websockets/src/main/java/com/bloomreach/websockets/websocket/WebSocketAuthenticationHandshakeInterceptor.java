package com.bloomreach.websockets.websocket;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.CONNECTION_ID;
import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID;
import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID_HEADER;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Authenticates WebSocket handshakes using the {@code X-User-Id} request header.
 *
 * <p>When the header is present, the user id and a unique connection id are stored on the
 * WebSocket session attributes.
 */
@Component
public class WebSocketAuthenticationHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Validates the handshake request and stores the user id and connection id on the session.
     *
     * @param request    HTTP upgrade request
     * @param response   HTTP upgrade response
     * @param wsHandler target WebSocket handler
     * @param attributes session attributes populated for the WebSocket connection
     * @return {@code true} when the request includes a non-blank user id; {@code false} otherwise
     */
    @Override
    public boolean beforeHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Map<String, Object> attributes) {
        final List<String> userIdHeaders = request.getHeaders().get(USER_ID_HEADER);
        if (userIdHeaders == null || userIdHeaders.isEmpty()) {
            return false;
        }

        final String userId = userIdHeaders.getFirst();
        if (userId == null || userId.isBlank()) {
            return false;
        }

        attributes.put(USER_ID, userId.trim());
        attributes.put(CONNECTION_ID, UUID.randomUUID().toString());
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
