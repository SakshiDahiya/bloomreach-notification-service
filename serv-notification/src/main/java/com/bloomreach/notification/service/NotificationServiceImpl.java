package com.bloomreach.notification.service;

import com.bloomreach.notification.service.model.*;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {


    @Override
    public NotificationResponse sendNotification(Notification notification) {
        validate(notification);

        return new NotificationResponse(UUID.randomUUID());
    }

    private void validate(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        if (notification.type() == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (notification.severity() == null) {
            throw new IllegalArgumentException("Notification severity cannot be null");
        }
        if (notification.type() == NotificationType.EMAIL) {
            if (!(notification.payload() instanceof EmailNotificationPayload)) {
                throw new IllegalArgumentException("Notification payload must be an EmailNotificationPayload");
            }
        } else if (notification.type() == NotificationType.WEBAPP) {
            if (!(notification.payload() instanceof WebappNotificationPayload)) {
                throw new IllegalArgumentException("Notification payload must be an WebappNotificationPayload");
            }
        }
    }

}
