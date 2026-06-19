package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationType;

public interface NotificationDeliveryAdaptor {

    NotificationType supportedType();

    void deliver(final NotificationEntity notificationEntity);
}
