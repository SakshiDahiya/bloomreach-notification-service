package com.bloomreach.notification.api;

import com.bloomreach.notification.generated.api.NotificationsApi;
import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.NotificationResponse;
import com.bloomreach.notification.service.NotificationService;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationSeverity;
import com.bloomreach.notification.service.model.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@Tag(name = "Notifications")
public class NotificationController implements NotificationsApi {

    private NotificationService notificationService;

    @Override
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Create a notification")
    @ApiResponse(responseCode = "202", description = "Notification accepted")
    @ApiResponse(responseCode = "400", description = "Invalid notification request")
    public ResponseEntity<NotificationResponse> createNotification(CreateNotificationRequest request) {
        notificationService.sendNotification(new Notification(
                NotificationType.valueOf(request.getDelivery().name()),
                NotificationSeverity.valueOf(request.getSeverity().name()),
                null,
                null
        ));
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
