package com.bloomreach.notification.service;

import com.bloomreach.notification.service.model.*;
import com.bloomreach.notification.service.delivery.NotificationDeliveryAdaptorRegistry;
import com.bloomreach.notification.service.repository.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAdaptorRegistry notificationDeliveryAdaptorRegistry;

    public NotificationServiceImpl(final NotificationRepository notificationRepository,
                                  final NotificationDeliveryAdaptorRegistry notificationDeliveryAdaptorRegistry) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryAdaptorRegistry = notificationDeliveryAdaptorRegistry;
    }

    @Override
    public NotificationResponse sendNotification(final Notification notification) {
        validate(notification);
        final NotificationEntity saved = notificationRepository.save(new NotificationEntity(UUID.randomUUID(), notification, Instant.now()));

        notificationDeliveryAdaptorRegistry.getAdaptor(notification.type()).deliver(saved);

        return new NotificationResponse(saved.id());
    }

    private void validate(final Notification notification) {
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
