package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.delivery.webapp.WebAppRequest;
import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebappNotificationDeliveryAdaptor implements NotificationDeliveryAdaptor {

    @Override
    public NotificationType supportedType() {
        return NotificationType.WEBAPP;
    }

    @Override
    public void deliver(final NotificationEntity notificationEntity) {
        // POC: actual webapp dispatch will be implemented later.
        WebappNotificationPayload payload =
                (WebappNotificationPayload) notificationEntity.notification().payload();
        final List<String> recipients = notificationEntity.notification().audience().userIds().stream()
                .toList();
        WebAppRequest webAppRequest = new WebAppRequest(
                notificationEntity.id(),
                recipients,
                payload
                );
        //call app-web-notification module to send notification
    }
}
