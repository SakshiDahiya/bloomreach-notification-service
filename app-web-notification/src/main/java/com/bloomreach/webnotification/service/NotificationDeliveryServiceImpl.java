package com.bloomreach.webnotification.service;

import static com.bloomreach.webnotification.websocket.WebSocketSessionAttributes.USER_ID;

import com.bloomreach.webnotification.model.WebAppNotificationMessage;
import com.bloomreach.webnotification.model.WebAppRequest;
import com.bloomreach.webnotification.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Default implementation of {@link NotificationDeliveryService}.
 *
 * <p>Delivers serialized notifications to active WebSocket sessions for each recipient user.
 * Delivery failures for individual sessions are logged and do not prevent delivery to other
 * sessions for the same user.
 */
@Service
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryServiceImpl.class);

    private final WebSocketSessionRegistry webSocketSessionRegistry;
    private final ObjectMapper objectMapper;

    /**
     * @param webSocketSessionRegistry registry of active WebSocket sessions by user id
     * @param objectMapper           Jackson mapper used to serialize notification messages
     */
    public NotificationDeliveryServiceImpl(
            final WebSocketSessionRegistry webSocketSessionRegistry,
            final ObjectMapper objectMapper) {
        this.webSocketSessionRegistry = webSocketSessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void deliver(final WebAppRequest webAppRequest) {
        final String serializedNotification = serializeNotification(webAppRequest);
        if (serializedNotification == null) {
            return;
        }

        for (final String recipientUserId : webAppRequest.recipientUserIds()) {
            deliverToRecipient(recipientUserId, serializedNotification);
        }
    }

    private void deliverToRecipient(final String recipientUserId, final String serializedNotification) {
        final Set<WebSocketSession> sessions = webSocketSessionRegistry.getSessions(recipientUserId);
        if (sessions.isEmpty()) {
            log.info("User {} not connected. Notification not delivered.", recipientUserId);
            return;
        }

        for (final WebSocketSession session : sessions) {
            deliverToSession(recipientUserId, session, serializedNotification);
        }
    }

    private void deliverToSession(
            final String recipientUserId,
            final WebSocketSession session,
            final String serializedNotification) {
        if (!session.isOpen()) {
            log.info("Skipping closed WebSocket session {} for user {}", session.getId(), recipientUserId);
            return;
        }

        final String sessionUserId = (String) session.getAttributes().get(USER_ID);
        if (!recipientUserId.equals(sessionUserId)) {
            log.warn(
                    "Skipping WebSocket session {} because it belongs to user {} instead of {}",
                    session.getId(),
                    sessionUserId,
                    recipientUserId
            );
            return;
        }

        try {
            session.sendMessage(new TextMessage(serializedNotification));
        } catch (IOException exception) {
            log.error(
                    "Failed to deliver notification to WebSocket session {} for user {}",
                    session.getId(),
                    recipientUserId,
                    exception
            );
        }
    }

    private String serializeNotification(final WebAppRequest webAppRequest) {
        final WebAppNotificationMessage message = new WebAppNotificationMessage(
                webAppRequest.notificationId(),
                webAppRequest.payload()
        );
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            log.error(
                    "Failed to serialize notification {} for delivery",
                    webAppRequest.notificationId(),
                    exception
            );
            return null;
        }
    }
}
