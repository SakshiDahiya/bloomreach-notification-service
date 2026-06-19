package com.bloomreach.websockets.websocket;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID;

import com.bloomreach.websockets.registry.WebSocketSessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket handler for persistent client sessions.
 *
 * <p>Registers authenticated sessions in {@link com.bloomreach.websockets.registry.WebSocketSessionRegistry}
 * when a connection is established and removes them on close or transport error.
 */
@Component
public class WebSocketSessionHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry webSocketSessionRegistry;

    /**
     * @param webSocketSessionRegistry registry of active WebSocket sessions by user id
     */
    public WebSocketSessionHandler(final WebSocketSessionRegistry webSocketSessionRegistry) {
        this.webSocketSessionRegistry = webSocketSessionRegistry;
    }

    /**
     * Registers the authenticated session after a successful WebSocket handshake.
     *
     * @param session established WebSocket session
     */
    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        final String userId = getUserId(session);
        webSocketSessionRegistry.register(userId, session);
    }

    /**
     * Removes the session when the WebSocket connection is closed.
     *
     * @param session established WebSocket session
     * @param status  close status supplied by the client or server
     */
    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        unregisterSession(session);
    }

    /**
     * Removes the session when a transport-level error occurs.
     *
     * @param session   established WebSocket session
     * @param exception transport error that occurred on the connection
     */
    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        unregisterSession(session);
    }

    private void unregisterSession(final WebSocketSession session) {
        final String userId = getUserId(session);
        if (userId != null) {
            webSocketSessionRegistry.remove(userId, session);
        }
    }

    private String getUserId(final WebSocketSession session) {
        return (String) session.getAttributes().get(USER_ID);
    }
}
