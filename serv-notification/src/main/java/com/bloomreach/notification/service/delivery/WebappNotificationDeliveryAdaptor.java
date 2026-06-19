package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class WebappNotificationDeliveryAdaptor implements NotificationDeliveryAdaptor {

    @Override
    public NotificationType supportedType() {
        return NotificationType.WEBAPP;
    }

    @Override
    public void deliver(final NotificationEntity notificationEntity) {
        // POC: actual webapp dispatch will be implemented later.
    }
}
