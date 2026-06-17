package com.bloomreach.notification.api;

import com.bloomreach.notification.generated.api.NotificationsApi;
import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notifications")
public class NotificationController implements NotificationsApi {

	@Override
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Create a notification")
	@ApiResponse(responseCode = "202", description = "Notification accepted")
	@ApiResponse(responseCode = "400", description = "Invalid notification request")
	public ResponseEntity<NotificationResponse> createNotification(CreateNotificationRequest request) {
		NotificationResponse response = new NotificationResponse()
				.id(UUID.randomUUID())
				.status("ACCEPTED")
				.title(request.getTitle())
				.severity(request.getSeverity())
				.delivery(request.getDelivery())
				.acceptedAt(OffsetDateTime.now());

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}
}
