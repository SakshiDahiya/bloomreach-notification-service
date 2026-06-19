package com.bloomreach.notification.service.delivery.webapp;

import java.util.Map;

/**
 * Request body for the app-websockets internal message API.
 *
 * @param userId  target user id
 * @param payload JSON payload delivered to active WebSocket connections
 */
public record SendMessageRequest(String userId, Map<String, Object> payload) {
}
