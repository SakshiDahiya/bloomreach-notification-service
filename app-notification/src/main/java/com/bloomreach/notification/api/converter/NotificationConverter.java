package com.bloomreach.notification.api.converter;

import com.bloomreach.notification.generated.model.CreateNotificationRequest;
import com.bloomreach.notification.generated.model.EmailPayload;
import com.bloomreach.notification.generated.model.CreateNotificationResponse;
import com.bloomreach.notification.generated.model.WebappPayload;
import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationPayload;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
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
