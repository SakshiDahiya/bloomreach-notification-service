package com.bloomreach.notification.service.repository;

import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    NotificationEntity save(final NotificationEntity entity);

    Optional<NotificationEntity> findById(final UUID id);

    List<NotificationEntity> findAll();

    NotificationUserDelivery saveUserDelivery(final NotificationUserDelivery delivery);

    Optional<NotificationUserDelivery> findUserDelivery(final UUID notificationId, final String userId);

    List<NotificationUserDelivery> findUserDeliveries(final UUID notificationId);

    List<NotificationUserDelivery> findUserDeliveriesByUserId(final String userId);
}
