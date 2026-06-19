package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.delivery.email.EmailProvider;
import com.bloomreach.notification.service.delivery.email.EmailRequest;
import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationDeliveryAdaptor implements NotificationDeliveryAdaptor {

    private final EmailProvider emailProvider;

    public EmailNotificationDeliveryAdaptor(final EmailProvider emailProvider) {
        this.emailProvider = emailProvider;
    }

    @Override
    public NotificationType supportedType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void deliver(final NotificationEntity notificationEntity) {
        final EmailNotificationPayload payload = (EmailNotificationPayload) notificationEntity.notification().payload();
        final List<String> recipients = notificationEntity.notification().audience().userIds().stream()
                .map(userId -> userId + "@notifications.local")
                .toList();

        emailProvider.send(new EmailRequest(
                notificationEntity.id(),
                recipients,
                payload
        ));
    }
}
