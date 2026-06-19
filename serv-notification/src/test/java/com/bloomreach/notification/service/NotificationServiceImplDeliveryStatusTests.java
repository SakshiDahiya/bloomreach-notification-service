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
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import com.bloomreach.notification.service.repository.InMemoryNotificationRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceImplDeliveryStatusTests {

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
    void recordsSentStatusForEachUserWhenNotificationIsCreated() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1", "user-2")));

        final List<NotificationUserDelivery> deliveries = repository.findUserDeliveries(response.id());
        assertEquals(2, deliveries.size());
        assertTrue(deliveries.stream().allMatch(delivery -> delivery.status() == DeliveryStatus.SENT));
    }

    @Test
    void recordsSentStatusForEmailNotifications() {
        final NotificationResponse response = service.sendNotification(emailNotification(List.of("user-1")));

        final NotificationUserDelivery delivery = repository.findUserDelivery(response.id(), "user-1").orElseThrow();
        assertEquals(DeliveryStatus.SENT, delivery.status());
    }

    @Test
    void updatesWebappDeliveryStatusToDelivered() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1")));

        final NotificationUserDelivery updated = service.updateDeliveryStatus(
                response.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.DELIVERED)
        );

        assertEquals(DeliveryStatus.DELIVERED, updated.status());
    }

    @Test
    void updatesWebappDeliveryStatusToRead() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1")));
        service.updateDeliveryStatus(response.id(), new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.DELIVERED));

        final NotificationUserDelivery updated = service.updateDeliveryStatus(
                response.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.READ)
        );

        assertEquals(DeliveryStatus.READ, updated.status());
    }

    @Test
    void doesNotDowngradeReadStatusWhenDeliveredArrivesLate() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1")));
        service.updateDeliveryStatus(response.id(), new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.READ));

        final NotificationUserDelivery updated = service.updateDeliveryStatus(
                response.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.DELIVERED)
        );

        assertEquals(DeliveryStatus.READ, updated.status());
    }

    @Test
    void keepsReadStatusWhenReadUpdateIsReceivedAgain() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1")));
        service.updateDeliveryStatus(response.id(), new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.READ));

        final NotificationUserDelivery updated = service.updateDeliveryStatus(
                response.id(),
                new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.READ)
        );

        assertEquals(DeliveryStatus.READ, updated.status());
    }

    @Test
    void rejectsDeliveryStatusUpdatesForEmailNotifications() {
        final NotificationResponse response = service.sendNotification(emailNotification(List.of("user-1")));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateDeliveryStatus(
                        response.id(),
                        new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.DELIVERED)
                )
        );
    }

    @Test
    void rejectsSentStatusInDeliveryStatusUpdateRequest() {
        final NotificationResponse response = service.sendNotification(webappNotification(List.of("user-1")));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateDeliveryStatus(
                        response.id(),
                        new UpdateDeliveryStatusRequest("user-1", DeliveryStatus.SENT)
                )
        );
    }

    private Notification webappNotification(final List<String> userIds) {
        return new Notification(
                NotificationType.WEBAPP,
                NotificationSeverity.INFO,
                new NotificationAudience(userIds, List.of(), Map.of()),
                new WebappNotificationPayload("Title", "Description", "Action")
        );
    }

    private Notification emailNotification(final List<String> userIds) {
        return new Notification(
                NotificationType.EMAIL,
                NotificationSeverity.INFO,
                new NotificationAudience(userIds, List.of(), Map.of()),
                new EmailNotificationPayload("Subject", "Body")
        );
    }
}
