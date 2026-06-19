package com.bloomreach.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationAudience;
import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationResponse;
import com.bloomreach.notification.service.model.NotificationSeverity;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.delivery.EmailNotificationDeliveryAdaptor;
import com.bloomreach.notification.service.delivery.NotificationDeliveryAdaptorRegistry;
import com.bloomreach.notification.service.delivery.WebappNotificationDeliveryAdaptor;
import com.bloomreach.notification.service.delivery.email.SendGridEmailProvider;
import com.bloomreach.notification.service.repository.InMemoryNotificationRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationServiceImplTests {

    @Test
    void persistsNotificationInMemoryAndReturnsUuid() {
        final InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
        final NotificationDeliveryAdaptorRegistry registry = new NotificationDeliveryAdaptorRegistry(
                List.of(
                        new EmailNotificationDeliveryAdaptor(new SendGridEmailProvider("noreply@example.com", "test-api-key")),
                        new WebappNotificationDeliveryAdaptor()
                )
        );
        final NotificationServiceImpl service = new NotificationServiceImpl(repository, registry);
        final Notification notification = new Notification(
                NotificationType.EMAIL,
                NotificationSeverity.INFO,
                new NotificationAudience(List.of("user-1"), List.of("admins"), Map.of("region", "us")),
                new EmailNotificationPayload("Subject", "Body")
        );

        final NotificationResponse response = service.sendNotification(notification);

        assertTrue(response.id() != null);
        final NotificationEntity saved = repository.findById(response.id()).orElseThrow();
        assertEquals(notification, saved.notification());
        assertEquals(response.id(), saved.id());
    }
}
