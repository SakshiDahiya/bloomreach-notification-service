package com.bloomreach.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bloomreach.notification.service.delivery.NotificationDeliveryAdaptorRegistry;
import com.bloomreach.notification.service.delivery.email.EmailNotificationDeliveryAdaptor;
import com.bloomreach.notification.service.delivery.email.SendGridEmailProvider;
import com.bloomreach.notification.service.delivery.webapp.SendMessageResponse;
import com.bloomreach.notification.service.delivery.webapp.WebAppNotificationDeliveryAdaptor;
import com.bloomreach.notification.service.model.DeliveryStatus;
import com.bloomreach.notification.service.model.EmailNotificationPayload;
import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationAudience;
import com.bloomreach.notification.service.model.NotificationResponse;
import com.bloomreach.notification.service.model.NotificationSeverity;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.PendingNotification;
import com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import com.bloomreach.notification.service.repository.InMemoryNotificationRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceImplPendingNotificationsTests {

    private InMemoryNotificationRepository repository;
    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNotificationRepository();
        service = new NotificationServiceImpl(
                repository,
                new NotificationDeliveryAdaptorRegistry(List.of(
                        new EmailNotificationDeliveryAdaptor(new SendGridEmailProvider("noreply@example.com", "test-api-key")),
                        new WebAppNotificationDeliveryAdaptor((userId, payload) ->
                                new SendMessageResponse(userId, false, 0, List.of()))
                ))
        );
    }

    @Test
    void returnsPendingWebappNotificationsWithSentStatus() {
        final NotificationResponse first = service.sendNotification(webappNotification("user-1", "First"));
        final NotificationResponse second = service.sendNotification(webappNotification("user-1", "Second"));
        service.sendNotification(webappNotification("user-2", "Other user"));

        final List<PendingNotification> pending = service.findPendingNotifications("user-1", NotificationType.WEBAPP);

        assertEquals(2, pending.size());
        assertTrue(pending.stream().allMatch(notification -> notification.status() == DeliveryStatus.SENT));
        assertEquals(first.id(), pending.get(0).id());
        assertEquals(second.id(), pending.get(1).id());
        assertEquals("First", pending.get(0).webappPayload().title());
        assertEquals("Second", pending.get(1).webappPayload().title());
    }

    @Test
    void excludesDeliveredAndReadNotifications() {
        final NotificationResponse sent = service.sendNotification(webappNotification("user-1", "Sent"));
        final NotificationResponse delivered = service.sendNotification(webappNotification("user-1", "Delivered"));
        final NotificationResponse read = service.sendNotification(webappNotification("user-1", "Read"));

        service.updateDeliveryStatus(
                delivered.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.DELIVERED)
        );
        service.updateDeliveryStatus(
                read.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.READ)
        );

        final List<PendingNotification> pending = service.findPendingNotifications("user-1", NotificationType.WEBAPP);

        assertEquals(1, pending.size());
        assertEquals(sent.id(), pending.getFirst().id());
    }

    @Test
    void rejectsNonWebappNotificationType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findPendingNotifications("user-1", NotificationType.EMAIL)
        );
    }

    private Notification webappNotification(final String userId, final String title) {
        return new Notification(
                NotificationType.WEBAPP,
                NotificationSeverity.INFO,
                new NotificationAudience(List.of(userId), List.of(), Map.of()),
                new WebappNotificationPayload(title, "Description", "Action")
        );
    }
}
