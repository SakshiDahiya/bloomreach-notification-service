package com.bloomreach.notification.service.model;

public record Notification(NotificationType type,
                           NotificationSeverity severity,
                           NotificationAudience audience,
                           NotificationPayload payload) {
}
