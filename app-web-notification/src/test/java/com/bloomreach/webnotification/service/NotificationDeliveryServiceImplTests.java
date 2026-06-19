package com.bloomreach.webnotification.service;

import static com.bloomreach.webnotification.websocket.WebSocketSessionAttributes.USER_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bloomreach.webnotification.model.WebAppNotificationPayload;
import com.bloomreach.webnotification.model.WebAppRequest;
import com.bloomreach.webnotification.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceImplTests {

    private static final UUID NOTIFICATION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private WebSocketSessionRegistry webSocketSessionRegistry;

    @Mock
    private WebSocketSession firstSession;

    @Mock
    private WebSocketSession secondSession;

    private NotificationDeliveryServiceImpl notificationDeliveryService;

    private ListAppender<ILoggingEvent> attachedLogAppender;

    @BeforeEach
    void setUp() {
        notificationDeliveryService = new NotificationDeliveryServiceImpl(
                webSocketSessionRegistry,
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        if (attachedLogAppender != null) {
            final Logger logger = (Logger) LoggerFactory.getLogger(NotificationDeliveryServiceImpl.class);
            logger.detachAppender(attachedLogAppender);
            attachedLogAppender.stop();
            attachedLogAppender = null;
        }
    }

    @Test
    void deliversSerializedNotificationToOpenSessionsForRecipient() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getId()).thenReturn("session-1");
        when(firstSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void deliversToAllActiveSessionsForSameUser() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession, secondSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(secondSession.isOpen()).thenReturn(true);
        when(firstSession.getId()).thenReturn("session-1");
        when(secondSession.getId()).thenReturn("session-2");
        when(firstSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));
        when(secondSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession).sendMessage(any(TextMessage.class));
        verify(secondSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void logsWhenUserIsNotConnected() {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of());

        final ListAppender<ILoggingEvent> logAppender = attachLogAppender();

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession, never()).sendMessage(any());
        assertTrue(containsLogMessage(logAppender, "User user-1 not connected. Notification not delivered."));
    }

    @Test
    void skipsClosedSessions() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(false);
        when(firstSession.getId()).thenReturn("session-1");

        final ListAppender<ILoggingEvent> logAppender = attachLogAppender();

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession, never()).sendMessage(any());
        assertTrue(containsLogMessage(logAppender, "Skipping closed WebSocket session session-1 for user user-1"));
    }

    @Test
    void doesNotDeliverToSessionsBelongingToOtherUsers() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getId()).thenReturn("session-1");
        when(firstSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-2"));

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession, never()).sendMessage(any());
    }

    @Test
    void continuesDeliveringWhenOneSessionFails() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession, secondSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(secondSession.isOpen()).thenReturn(true);
        when(firstSession.getId()).thenReturn("session-1");
        when(secondSession.getId()).thenReturn("session-2");
        when(firstSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));
        when(secondSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));
        doThrow(new IOException("send failed")).when(firstSession).sendMessage(any(TextMessage.class));

        notificationDeliveryService.deliver(requestFor("user-1"));

        verify(firstSession).sendMessage(any(TextMessage.class));
        verify(secondSession).sendMessage(any(TextMessage.class));
    }

    @Test
    void serializesNotificationPayloadAsJson() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getId()).thenReturn("session-1");
        when(firstSession.getAttributes()).thenReturn(Map.of(USER_ID, "user-1"));

        notificationDeliveryService.deliver(requestFor("user-1"));

        final ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(firstSession, times(1)).sendMessage(messageCaptor.capture());

        final String payload = messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("Maintenance"));
        assertTrue(payload.contains(NOTIFICATION_ID.toString()));
    }

    private WebAppRequest requestFor(final String... recipientUserIds) {
        return new WebAppRequest(
                NOTIFICATION_ID,
                List.of(recipientUserIds),
                new WebAppNotificationPayload("Maintenance", "System unavailable tonight", "View details")
        );
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        final Logger logger = (Logger) LoggerFactory.getLogger(NotificationDeliveryServiceImpl.class);
        attachedLogAppender = new ListAppender<>();
        attachedLogAppender.start();
        logger.addAppender(attachedLogAppender);
        return attachedLogAppender;
    }

    private boolean containsLogMessage(final ListAppender<ILoggingEvent> appender, final String message) {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message::equals);
    }
}
