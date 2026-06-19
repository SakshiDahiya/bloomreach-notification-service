package com.bloomreach.webnotification.model;

import java.util.List;
import java.util.UUID;

/**
 * Request to deliver a web notification to one or more users.
 *
 * @param notificationId     unique notification identifier
 * @param recipientUserIds   user ids that should receive the notification
 * @param payload            web notification content
 */
public record WebAppRequest(
        UUID notificationId,
        List<String> recipientUserIds,
        WebAppNotificationPayload payload) {
}
