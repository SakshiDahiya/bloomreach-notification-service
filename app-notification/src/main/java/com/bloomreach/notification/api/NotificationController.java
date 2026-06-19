package com.bloomreach.notification.api;

import com.bloomreach.notification.generated.api.NotificationsApi;
import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.CreateNotificationResponse;
import com.bloomreach.notification.generated.model.NotificationType;
import com.bloomreach.notification.generated.model.NotificationUserDeliveryResponse;
import com.bloomreach.notification.generated.model.PendingNotificationsResponse;
import com.bloomreach.notification.generated.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.service.NotificationService;
import com.bloomreach.notification.service.model.NotificationResponse;
import static com.bloomreach.notification.api.converter.NotificationConverter.toDeliveryStatusResponse;
import static com.bloomreach.notification.api.converter.NotificationConverter.toNotification;
import static com.bloomreach.notification.api.converter.NotificationConverter.toNotificationType;
import static com.bloomreach.notification.api.converter.NotificationConverter.toPendingNotificationsResponse;
import static com.bloomreach.notification.api.converter.NotificationConverter.toResponse;
import static com.bloomreach.notification.api.converter.NotificationConverter.toUpdateDeliveryStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notifications")
public class NotificationController implements NotificationsApi {

    private final NotificationService notificationService;

    public NotificationController(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Create a notification")
    @ApiResponse(responseCode = "202", description = "Notification accepted")
    @ApiResponse(responseCode = "400", description = "Invalid notification request")
    public ResponseEntity<CreateNotificationResponse> createNotification(final CreateNotificationRequest request) {
        final NotificationResponse notificationResponse = notificationService.sendNotification(toNotification(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(notificationResponse));
    }

    @Override
    @Operation(summary = "Update notification delivery status for a user")
    @ApiResponse(responseCode = "200", description = "Delivery status updated or returned unchanged")
    @ApiResponse(responseCode = "400", description = "Invalid delivery status request")
    public ResponseEntity<NotificationUserDeliveryResponse> updateNotificationDeliveryStatus(
            final UUID notificationId,
            final UpdateDeliveryStatusRequest updateDeliveryStatusRequest) {
        return ResponseEntity.ok(toDeliveryStatusResponse(
                notificationService.updateDeliveryStatus(notificationId, toUpdateDeliveryStatusRequest(updateDeliveryStatusRequest))
        ));
    }

    @Override
    @Operation(summary = "Fetch pending notifications for a user")
    @ApiResponse(responseCode = "200", description = "Pending notifications for the user")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<PendingNotificationsResponse> getPendingNotifications(
            final String userId,
            final NotificationType notificationType) {
        return ResponseEntity.ok(toPendingNotificationsResponse(
                notificationService.findPendingNotifications(userId, toNotificationType(notificationType))
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
