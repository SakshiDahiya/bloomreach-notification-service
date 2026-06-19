package com.bloomreach.webnotification.websocket;

/**
 * Constants for attributes stored on {@link org.springframework.web.socket.WebSocketSession}
 * instances.
 */
public final class WebSocketSessionAttributes {

    /**
     * Session attribute key for the authenticated user id.
     */
    public static final String USER_ID = "userId";

    private WebSocketSessionAttributes() {
    }
}
