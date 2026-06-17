package com.bloomreach.notification.service.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Notification creation response")
public record NotificationResponse(
		UUID id,
		String status,
		String title,
		NotificationSeverity severity,
		NotificationDelivery delivery,
		OffsetDateTime acceptedAt) {
}
