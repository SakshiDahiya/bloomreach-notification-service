package com.bloomreach.notification.service.delivery.email;

import java.util.List;
import java.util.UUID;

public record EmailRequest(
        UUID notificationId,
        List<String> recipients,
        String subject,
        String body) {
}
