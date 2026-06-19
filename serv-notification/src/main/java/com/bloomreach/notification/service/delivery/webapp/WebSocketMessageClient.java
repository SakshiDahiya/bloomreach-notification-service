package com.bloomreach.notification.service.delivery.webapp;

import java.util.Map;

/**
 * Client for the app-websockets internal message API.
 */
public interface WebSocketMessageClient {

    /**
     * Sends a JSON payload to every active WebSocket connection for the given user.
     *
     * @param userId  target user id
     * @param payload JSON payload to deliver
     * @return delivery result from the websockets service
     */
    SendMessageResponse sendMessage(String userId, Map<String, Object> payload);
}
