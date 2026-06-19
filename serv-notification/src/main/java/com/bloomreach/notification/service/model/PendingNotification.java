package com.bloomreach.notification.service.model;

import java.time.Instant;
import java.util.UUID;

/**
 * A notification that has not yet been delivered or read by a user.
 */
public record PendingNotification(
        UUID id,
        NotificationSeverity severity,
        DeliveryStatus status,
        Instant createdAt,
        WebappNotificationPayload webappPayload) {
}
