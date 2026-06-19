package com.bloomreach.notification.service.repository;

import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final ConcurrentMap<UUID, NotificationEntity> storage = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, NotificationUserDelivery> deliveryStorage = new ConcurrentHashMap<>();

    @Override
    public NotificationEntity save(final NotificationEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Notification entity cannot be null");
        }

        storage.put(entity.id(), entity);
        return entity;
    }

    @Override
    public Optional<NotificationEntity> findById(final UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<NotificationEntity> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public NotificationUserDelivery saveUserDelivery(final NotificationUserDelivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Notification user delivery cannot be null");
        }

        deliveryStorage.put(deliveryKey(delivery.notificationId(), delivery.userId()), delivery);
        return delivery;
    }

    @Override
    public Optional<NotificationUserDelivery> findUserDelivery(final UUID notificationId, final String userId) {
        return Optional.ofNullable(deliveryStorage.get(deliveryKey(notificationId, userId)));
    }

    @Override
    public List<NotificationUserDelivery> findUserDeliveries(final UUID notificationId) {
        return deliveryStorage.values().stream()
                .filter(delivery -> delivery.notificationId().equals(notificationId))
                .toList();
    }

    @Override
    public List<NotificationUserDelivery> findUserDeliveriesByUserId(final String userId) {
        return deliveryStorage.values().stream()
                .filter(delivery -> delivery.userId().equals(userId))
                .toList();
    }

    private String deliveryKey(final UUID notificationId, final String userId) {
        return notificationId + ":" + userId;
    }
}
