package com.bloomreach.websockets.service;

import com.bloomreach.websockets.generated.model.SendMessageResponse;
import java.util.Map;

/**
 * Service for delivering messages to connected web clients over WebSocket.
 */
public interface WebSocketMessageService {

    /**
     * Delivers a JSON payload to the active WebSocket sessions of the given user.
     *
     * @param userId  recipient user id
     * @param payload JSON payload to send
     * @return delivery result including connection ids that received the payload
     */
    SendMessageResponse send(String userId, Map<String, Object> payload);
}
