package com.bloomreach.notification.service;

import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationResponse;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import com.bloomreach.notification.service.model.PendingNotification;
import com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(final Notification notification);

    NotificationUserDelivery updateDeliveryStatus(
            final UUID notificationId,
            final UpdateDeliveryStatusRequest request);

    List<PendingNotification> findPendingNotifications(final String userId, final NotificationType notificationType);
}
