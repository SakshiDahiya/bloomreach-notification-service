package com.bloomreach.notification.api.converter;

import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.EmailPayload;
import com.bloomreach.notification.generated.model.CreateNotificationResponse;
import com.bloomreach.notification.generated.model.NotificationUserDeliveryResponse;
import com.bloomreach.notification.generated.model.PendingNotificationsResponse;
import com.bloomreach.notification.generated.model.PendingWebappNotification;
import com.bloomreach.notification.generated.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.generated.model.WebappPayload;
import com.bloomreach.notification.service.model.DeliveryStatus;
import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationPayload;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import com.bloomreach.notification.service.model.PendingNotification;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
public final class NotificationConverter {

    private NotificationConverter() {
    }

    public static Notification toNotification(final CreateNotificationRequest request) {
        return new Notification(
                com.bloomreach.notification.service.model.NotificationType.valueOf(request.getNotificationType().name()),
                com.bloomreach.notification.service.model.NotificationSeverity.valueOf(request.getSeverity().name()),
                new com.bloomreach.notification.service.model.NotificationAudience(
                        request.getAudience().getUserIds(),
                        request.getAudience().getGroups(),
                        request.getAudience().getLabels()
                ),
                toPayload(request)
        );
    }

    public static CreateNotificationResponse toResponse(final com.bloomreach.notification.service.model.NotificationResponse response) {
        return new CreateNotificationResponse()
                .id(response.id());
    }

    public static com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest toUpdateDeliveryStatusRequest(
            final UpdateDeliveryStatusRequest request) {
        return new com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest(
                request.getUserId(),
                DeliveryStatus.valueOf(request.getStatus().name())
        );
    }

    public static NotificationUserDeliveryResponse toDeliveryStatusResponse(
            final NotificationUserDelivery delivery) {
        return new NotificationUserDeliveryResponse()
                .notificationId(delivery.notificationId())
                .userId(delivery.userId())
                .status(com.bloomreach.notification.generated.model.DeliveryStatus.valueOf(delivery.status().name()))
                .updatedAt(OffsetDateTime.ofInstant(delivery.updatedAt(), ZoneOffset.UTC));
    }

    public static PendingNotificationsResponse toPendingNotificationsResponse(
            final List<PendingNotification> pendingNotifications) {
        return new PendingNotificationsResponse()
                .notifications(pendingNotifications.stream()
                        .map(NotificationConverter::toPendingWebappNotification)
                        .toList());
    }

    public static com.bloomreach.notification.service.model.NotificationType toNotificationType(
            final com.bloomreach.notification.generated.model.NotificationType notificationType) {
        return com.bloomreach.notification.service.model.NotificationType.valueOf(notificationType.name());
    }

    private static PendingWebappNotification toPendingWebappNotification(final PendingNotification pendingNotification) {
        return new PendingWebappNotification()
                .id(pendingNotification.id())
                .severity(com.bloomreach.notification.generated.model.NotificationSeverity.valueOf(
                        pendingNotification.severity().name()))
                .status(com.bloomreach.notification.generated.model.DeliveryStatus.valueOf(
                        pendingNotification.status().name()))
                .createdAt(OffsetDateTime.ofInstant(pendingNotification.createdAt(), ZoneOffset.UTC))
                .webappPayload(toWebappPayload(pendingNotification.webappPayload()));
    }

    private static WebappPayload toWebappPayload(final WebappNotificationPayload payload) {
        return new WebappPayload()
                .title(payload.title())
                .description(payload.description())
                .action(payload.action());
    }

    private static NotificationPayload toPayload(final CreateNotificationRequest request) {
        if (request.getNotificationType() == null) {
            throw new IllegalArgumentException("notificationType is required");
        }

        return switch (request.getNotificationType()) {
            case EMAIL -> emailPayload(request.getEmailPayload());
            case WEBAPP -> webappPayload(request.getWebappPayload());
        };
    }

    private static NotificationPayload emailPayload(final EmailPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("emailPayload is required for EMAIL delivery");
        }
        return new EmailNotificationPayload(payload.getSubject(), payload.getBody());
    }

    private static NotificationPayload webappPayload(final WebappPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("webappPayload is required for WEBAPP delivery");
        }
        return new WebappNotificationPayload(payload.getTitle(), payload.getDescription(), payload.getAction());
    }
}
