package com.bloomreach.notification.service.delivery.webapp;

import com.bloomreach.notification.service.delivery.NotificationDeliveryAdaptor;
import com.bloomreach.notification.service.model.NotificationEntity;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class WebAppNotificationDeliveryAdaptor implements NotificationDeliveryAdaptor {

    private static final Logger log = LoggerFactory.getLogger(WebAppNotificationDeliveryAdaptor.class);

    private final WebSocketMessageClient webSocketMessageClient;

    public WebAppNotificationDeliveryAdaptor(final WebSocketMessageClient webSocketMessageClient) {
        this.webSocketMessageClient = webSocketMessageClient;
    }

    @Override
    public NotificationType supportedType() {
        return NotificationType.WEBAPP;
    }

    @Override
    public void deliver(final NotificationEntity notificationEntity) {
        final WebappNotificationPayload payload =
                (WebappNotificationPayload) notificationEntity.notification().payload();
        final List<String> recipients = notificationEntity.notification().audience().userIds();
        final Map<String, Object> messagePayload = toMessagePayload(notificationEntity, payload);

        for (final String userId : recipients) {
            deliverToUser(userId, messagePayload);
        }
    }

    private void deliverToUser(final String userId, final Map<String, Object> messagePayload) {
        try {
            webSocketMessageClient.sendMessage(userId, messagePayload);
        } catch (final RestClientException exception) {
            log.error("Failed to deliver webapp message to user {}", userId, exception);
        }
    }

    private Map<String, Object> toMessagePayload(
            final NotificationEntity notificationEntity,
            final WebappNotificationPayload payload) {
        final Map<String, Object> messagePayload = new LinkedHashMap<>();
        messagePayload.put("notificationId", notificationEntity.id().toString());
        messagePayload.put("title", payload.title());
        messagePayload.put("description", payload.description());
        messagePayload.put("action", payload.action());
        return messagePayload;
    }
}
