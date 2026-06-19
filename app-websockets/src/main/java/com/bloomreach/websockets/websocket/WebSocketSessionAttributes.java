package com.bloomreach.websockets.websocket;

/**
 * Constants for attributes stored on {@link org.springframework.web.socket.WebSocketSession}
 * instances.
 */
public final class WebSocketSessionAttributes {

    /**
     * Session attribute key for the authenticated user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Session attribute key for the unique connection id assigned at handshake time.
     */
    public static final String CONNECTION_ID = "connectionId";

    /**
     * HTTP header used by frontend clients to identify the user during the WebSocket handshake.
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    private WebSocketSessionAttributes() {
    }
}
