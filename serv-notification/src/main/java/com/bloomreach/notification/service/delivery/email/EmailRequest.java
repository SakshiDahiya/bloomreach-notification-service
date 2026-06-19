package com.bloomreach.notification.service.delivery.email;

import com.bloomreach.notification.service.model.EmailNotificationPayload;

import java.util.List;
import java.util.UUID;

public record EmailRequest(
        UUID notificationId,
        List<String> recipients,
        EmailNotificationPayload payload) {
}
