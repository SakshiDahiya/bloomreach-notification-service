package com.bloomreach.notification.service.repository;

import com.bloomreach.notification.service.model.NotificationEntity;
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
}
