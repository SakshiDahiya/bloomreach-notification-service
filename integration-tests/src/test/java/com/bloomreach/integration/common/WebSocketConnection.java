package com.bloomreach.integration.common;

import org.springframework.web.socket.WebSocketSession;

public record WebSocketConnection(WebSocketSession session, CollectingWebSocketHandler handler) {
}
