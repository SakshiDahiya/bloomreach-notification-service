package com.bloomreach.notification.api;

import com.bloomreach.notification.generated.api.NotificationsApi;
import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.CreateNotificationResponse;
import com.bloomreach.notification.service.NotificationService;
import com.bloomreach.notification.service.model.NotificationResponse;
import static com.bloomreach.notification.api.converter.NotificationConverter.toNotification;
import static com.bloomreach.notification.api.converter.NotificationConverter.toResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Notifications")
public class NotificationController implements NotificationsApi {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Create a notification")
    @ApiResponse(responseCode = "202", description = "Notification accepted")
    @ApiResponse(responseCode = "400", description = "Invalid notification request")
    public ResponseEntity<CreateNotificationResponse> createNotification(CreateNotificationRequest request) {
        NotificationResponse notificationResponse = notificationService.sendNotification(toNotification(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(notificationResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
