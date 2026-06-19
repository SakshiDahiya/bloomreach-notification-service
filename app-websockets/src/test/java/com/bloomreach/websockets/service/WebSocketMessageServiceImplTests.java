package com.bloomreach.websockets.service;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.CONNECTION_ID;
import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bloomreach.websockets.generated.model.SendMessageResponse;
import com.bloomreach.websockets.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
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
class WebSocketMessageServiceImplTests {

    private static final Map<String, Object> PAYLOAD = Map.of(
            "title", "Maintenance",
            "description", "System unavailable tonight",
            "action", "View details"
    );

    @Mock
    private WebSocketSessionRegistry webSocketSessionRegistry;

    @Mock
    private WebSocketSession firstSession;

    @Mock
    private WebSocketSession secondSession;

    private WebSocketMessageServiceImpl webSocketMessageService;

    private ListAppender<ILoggingEvent> attachedLogAppender;

    @BeforeEach
    void setUp() {
        webSocketMessageService = new WebSocketMessageServiceImpl(
                webSocketSessionRegistry,
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        if (attachedLogAppender != null) {
            final Logger logger = (Logger) LoggerFactory.getLogger(WebSocketMessageServiceImpl.class);
            logger.detachAppender(attachedLogAppender);
            attachedLogAppender.stop();
            attachedLogAppender = null;
        }
    }

    @Test
    void deliversSerializedPayloadToOpenSessionsForRecipient() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-1"
        ));

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession).sendMessage(any(TextMessage.class));
        assertTrue(response.getConnected());
        assertEquals(1, response.getDeliveredConnectionCount());
        assertEquals("connection-1", response.getConnectionIds().getFirst());
    }

    @Test
    void deliversToAllActiveSessionsForSameUser() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession, secondSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(secondSession.isOpen()).thenReturn(true);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-1"
        ));
        when(secondSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-2"
        ));

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession).sendMessage(any(TextMessage.class));
        verify(secondSession).sendMessage(any(TextMessage.class));
        assertEquals(2, response.getDeliveredConnectionCount());
    }

    @Test
    void returnsNotConnectedWhenUserHasNoSessions() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of());

        final ListAppender<ILoggingEvent> logAppender = attachLogAppender();

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession, never()).sendMessage(any());
        assertFalse(response.getConnected());
        assertEquals(0, response.getDeliveredConnectionCount());
        assertTrue(containsLogMessage(logAppender, "User user-1 not connected. Message not delivered."));
    }

    @Test
    void skipsClosedSessions() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(false);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-1"
        ));

        final ListAppender<ILoggingEvent> logAppender = attachLogAppender();

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession, never()).sendMessage(any());
        assertTrue(response.getConnected());
        assertEquals(0, response.getDeliveredConnectionCount());
        assertTrue(containsLogMessage(
                logAppender,
                "Skipping closed WebSocket session connection-1 for user user-1"
        ));
    }

    @Test
    void doesNotDeliverToSessionsBelongingToOtherUsers() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-2",
                CONNECTION_ID, "connection-1"
        ));

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession, never()).sendMessage(any());
        assertEquals(0, response.getDeliveredConnectionCount());
    }

    @Test
    void continuesDeliveringWhenOneSessionFails() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession, secondSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(secondSession.isOpen()).thenReturn(true);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-1"
        ));
        when(secondSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-2"
        ));
        doThrow(new IOException("send failed")).when(firstSession).sendMessage(any(TextMessage.class));

        final SendMessageResponse response = webSocketMessageService.send("user-1", PAYLOAD);

        verify(firstSession).sendMessage(any(TextMessage.class));
        verify(secondSession).sendMessage(any(TextMessage.class));
        assertEquals(1, response.getDeliveredConnectionCount());
        assertEquals("connection-2", response.getConnectionIds().getFirst());
    }

    @Test
    void serializesPayloadAsJson() throws IOException {
        when(webSocketSessionRegistry.getSessions("user-1")).thenReturn(Set.of(firstSession));
        when(firstSession.isOpen()).thenReturn(true);
        when(firstSession.getAttributes()).thenReturn(Map.of(
                USER_ID, "user-1",
                CONNECTION_ID, "connection-1"
        ));

        webSocketMessageService.send("user-1", PAYLOAD);

        final ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(firstSession).sendMessage(messageCaptor.capture());

        final String payload = messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("Maintenance"));
        assertTrue(payload.contains("View details"));
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        final Logger logger = (Logger) LoggerFactory.getLogger(WebSocketMessageServiceImpl.class);
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
