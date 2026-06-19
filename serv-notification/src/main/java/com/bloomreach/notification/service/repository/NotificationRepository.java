package com.bloomreach.notification.service.repository;

import com.bloomreach.notification.service.model.NotificationEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    NotificationEntity save(final NotificationEntity entity);

    Optional<NotificationEntity> findById(final UUID id);

    List<NotificationEntity> findAll();
}
