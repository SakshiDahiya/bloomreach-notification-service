package com.bloomreach.notification.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a notification")
public record CreateNotificationRequest(
		@Schema(description = "Notification title", example = "Scheduled maintenance")
		@NotBlank
		String title,

		@Schema(description = "Notification message", example = "The system will be unavailable tonight from 11 PM to 12 AM.")
		@NotBlank
		String message,

		@Schema(description = "Notification severity", example = "INFO")
		@NotNull
		NotificationSeverity severity,

		@Schema(description = "Notification delivery channel", example = "EMAIL")
		@NotNull
		NotificationDelivery delivery,

		@Valid
		@NotNull
		AudienceRequest audience) {
}
