package com.bloomreach.notification.service.model;

public record WebappNotificationPayload(String title,
                                        String description,
                                        String action) implements NotificationPayload {
}
