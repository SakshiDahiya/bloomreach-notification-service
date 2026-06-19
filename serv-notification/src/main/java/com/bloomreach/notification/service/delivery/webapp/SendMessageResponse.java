package com.bloomreach.notification.service.delivery.webapp;

import java.util.List;

/**
 * Response body from the app-websockets internal message API.
 *
 * @param userId                   target user id
 * @param connected                whether the user had active WebSocket connections
 * @param deliveredConnectionCount number of connections that received the payload
 * @param connectionIds            connection ids that received the payload
 */
public record SendMessageResponse(
        String userId,
        boolean connected,
        int deliveredConnectionCount,
        List<String> connectionIds) {
}
