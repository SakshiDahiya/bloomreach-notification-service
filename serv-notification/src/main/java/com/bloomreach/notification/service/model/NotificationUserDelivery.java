package com.bloomreach.notification.service.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Delivery status of a notification for a specific user.
 */
public record NotificationUserDelivery(
        UUID notificationId,
        String userId,
        DeliveryStatus status,
        Instant updatedAt) {
}
