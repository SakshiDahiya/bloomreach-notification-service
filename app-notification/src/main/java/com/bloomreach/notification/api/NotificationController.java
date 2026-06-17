package com.bloomreach.notification.api;

import com.bloomreach.notification.api.model.CreateNotificationRequest;
import com.bloomreach.notification.api.model.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Create a notification")
	@ApiResponse(responseCode = "202", description = "Notification accepted")
	@ApiResponse(responseCode = "400", description = "Invalid notification request")
	public NotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
		return new NotificationResponse(
				UUID.randomUUID(),
				"ACCEPTED",
				request.title(),
				request.severity(),
				request.delivery(),
				OffsetDateTime.now());
	}
}
