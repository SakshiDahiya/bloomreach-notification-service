package com.bloomreach.notification.service.delivery.webapp;

import com.bloomreach.notification.service.model.WebappNotificationPayload;

import java.util.List;
import java.util.UUID;

public record WebAppRequest(
        UUID notificationId,
        List<String> recipients,
        WebappNotificationPayload payload) {
}
