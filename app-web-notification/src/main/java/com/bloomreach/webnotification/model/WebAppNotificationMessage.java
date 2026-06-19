package com.bloomreach.webnotification.model;

import java.util.UUID;

/**
 * Notification message serialized and sent over WebSocket to connected clients.
 *
 * @param notificationId unique notification identifier
 * @param payload        web notification content
 */
public record WebAppNotificationMessage(UUID notificationId, WebAppNotificationPayload payload) {
}
