package com.bloomreach.notification.service.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationEntity(
        UUID id,
        Notification notification,
        Instant createdAt) {
}
