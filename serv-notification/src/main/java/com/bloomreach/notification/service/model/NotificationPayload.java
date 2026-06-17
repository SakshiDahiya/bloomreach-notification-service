package com.bloomreach.notification.service.model;

public sealed interface NotificationPayload permits EmailNotificationPayload, WebappNotificationPayload {
}
