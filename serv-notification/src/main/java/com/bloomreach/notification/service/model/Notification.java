package com.bloomreach.notification.service.model;

import java.time.OffsetDateTime;

public record Notification(
        String status,
        String title,
        String content,
        NotificationSeverity severity,
        NotificationDelivery delivery,
        OffsetDateTime acceptedAt,
        NotificationAudience notificationAudience) {

}
