package com.bloomreach.notification.service;

import com.bloomreach.notification.service.delivery.NotificationDeliveryAdaptorRegistry;
import com.bloomreach.notification.service.model.DeliveryStatus;
import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationResponse;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import com.bloomreach.notification.service.model.PendingNotification;
import com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import com.bloomreach.notification.service.repository.NotificationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAdaptorRegistry notificationDeliveryAdaptorRegistry;

    public NotificationServiceImpl(
            final NotificationRepository notificationRepository,
            final NotificationDeliveryAdaptorRegistry notificationDeliveryAdaptorRegistry) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryAdaptorRegistry = notificationDeliveryAdaptorRegistry;
    }

    @Override
    public NotificationResponse sendNotification(final Notification notification) {
        validate(notification);
        final NotificationEntity saved = notificationRepository.save(
                new NotificationEntity(UUID.randomUUID(), notification, Instant.now())
        );
        recordInitialDeliveryStatuses(saved);
        notificationDeliveryAdaptorRegistry.getAdaptor(notification.type()).deliver(saved);
        return new NotificationResponse(saved.id());
    }

    @Override
    public NotificationUserDelivery updateDeliveryStatus(
            final UUID notificationId,
            final UpdateDeliveryStatusRequest request) {
        validateDeliveryStatusUpdateRequest(request);

        final NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notificationEntity.notification().audience().userIds().contains(request.userId())) {
            throw new IllegalArgumentException("User is not in the notification audience: " + request.userId());
        }

        if (notificationEntity.notification().type() != NotificationType.WEBAPP) {
            throw new IllegalArgumentException("Delivery status updates are only supported for WEBAPP notifications");
        }

        final NotificationUserDelivery existingDelivery = notificationRepository
                .findUserDelivery(notificationId, request.userId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery status not found for notification " + notificationId + " and user " + request.userId()
                ));

        if (existingDelivery.status() == DeliveryStatus.READ) {
            return existingDelivery;
        }

        if (!shouldAdvanceStatus(existingDelivery.status(), request.status())) {
            return existingDelivery;
        }

        return notificationRepository.saveUserDelivery(new NotificationUserDelivery(
                notificationId,
                request.userId(),
                request.status(),
                Instant.now()
        ));
    }

    @Override
    public List<PendingNotification> findPendingNotifications(
            final String userId,
            final NotificationType notificationType) {
        validatePendingNotificationsRequest(userId, notificationType);

        final List<PendingNotification> pendingNotifications = new ArrayList<>();
        for (final NotificationUserDelivery delivery : notificationRepository.findUserDeliveriesByUserId(userId)) {
            if (delivery.status() != DeliveryStatus.SENT) {
                continue;
            }

            final NotificationEntity notificationEntity = notificationRepository.findById(delivery.notificationId())
                    .orElse(null);
            if (notificationEntity == null || notificationEntity.notification().type() != notificationType) {
                continue;
            }

            pendingNotifications.add(toPendingNotification(notificationEntity, delivery));
        }

        pendingNotifications.sort(Comparator.comparing(PendingNotification::createdAt));
        return pendingNotifications;
    }

    private PendingNotification toPendingNotification(
            final NotificationEntity notificationEntity,
            final NotificationUserDelivery delivery) {
        return new PendingNotification(
                notificationEntity.id(),
                notificationEntity.notification().severity(),
                delivery.status(),
                notificationEntity.createdAt(),
                (WebappNotificationPayload) notificationEntity.notification().payload()
        );
    }

    private void validatePendingNotificationsRequest(final String userId, final NotificationType notificationType) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (notificationType == null) {
            throw new IllegalArgumentException("notificationType is required");
        }
        if (notificationType != NotificationType.WEBAPP) {
            throw new IllegalArgumentException("Only WEBAPP notifications are supported");
        }
    }

    private void recordInitialDeliveryStatuses(final NotificationEntity saved) {
        final Instant now = Instant.now();
        for (final String userId : saved.notification().audience().userIds()) {
            notificationRepository.saveUserDelivery(new NotificationUserDelivery(
                    saved.id(),
                    userId,
                    DeliveryStatus.SENT,
                    now
            ));
        }
    }

    private void validateDeliveryStatusUpdateRequest(final UpdateDeliveryStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery status update request cannot be null");
        }
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.status() == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (request.status() != DeliveryStatus.DELIVERED && request.status() != DeliveryStatus.READ) {
            throw new IllegalArgumentException("status must be DELIVERED or READ");
        }
    }

    private boolean shouldAdvanceStatus(final DeliveryStatus current, final DeliveryStatus requested) {
        return requested.ordinal() > current.ordinal();
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
