package com.bloomreach.websockets.service;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.CONNECTION_ID;
import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID;

import com.bloomreach.websockets.generated.model.SendMessageResponse;
import com.bloomreach.websockets.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Default implementation of {@link WebSocketMessageService}.
 *
 * <p>Delivers serialized JSON payloads to active WebSocket sessions for the target user.
 * Delivery failures for individual sessions are logged and do not prevent delivery to other
 * sessions for the same user.
 */
@Service
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageServiceImpl.class);

    private final WebSocketSessionRegistry webSocketSessionRegistry;
    private final ObjectMapper objectMapper;

    /**
     * @param webSocketSessionRegistry registry of active WebSocket sessions by user id
     * @param objectMapper           Jackson mapper used to serialize message payloads
     */
    public WebSocketMessageServiceImpl(
            final WebSocketSessionRegistry webSocketSessionRegistry,
            final ObjectMapper objectMapper) {
        this.webSocketSessionRegistry = webSocketSessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public SendMessageResponse send(final String userId, final Map<String, Object> payload) {
        final Set<WebSocketSession> sessions = webSocketSessionRegistry.getSessions(userId);
        if (sessions.isEmpty()) {
            log.info("User {} not connected. Message not delivered.", userId);
            return new SendMessageResponse()
                    .userId(userId)
                    .connected(false)
                    .deliveredConnectionCount(0)
                    .connectionIds(List.of());
        }

        final String serializedPayload = serializePayload(userId, payload);
        if (serializedPayload == null) {
            return new SendMessageResponse()
                    .userId(userId)
                    .connected(true)
                    .deliveredConnectionCount(0)
                    .connectionIds(List.of());
        }

        final List<String> deliveredConnectionIds = new ArrayList<>();
        for (final WebSocketSession session : sessions) {
            if (deliverToSession(userId, session, serializedPayload)) {
                deliveredConnectionIds.add(getConnectionId(session));
            }
        }

        return new SendMessageResponse()
                .userId(userId)
                .connected(true)
                .deliveredConnectionCount(deliveredConnectionIds.size())
                .connectionIds(deliveredConnectionIds);
    }

    private boolean deliverToSession(
            final String recipientUserId,
            final WebSocketSession session,
            final String serializedPayload) {
        if (!session.isOpen()) {
            log.info(
                    "Skipping closed WebSocket session {} for user {}",
                    getConnectionId(session),
                    recipientUserId
            );
            return false;
        }

        final String sessionUserId = (String) session.getAttributes().get(USER_ID);
        if (!recipientUserId.equals(sessionUserId)) {
            log.warn(
                    "Skipping WebSocket session {} because it belongs to user {} instead of {}",
                    getConnectionId(session),
                    sessionUserId,
                    recipientUserId
            );
            return false;
        }

        try {
            session.sendMessage(new TextMessage(serializedPayload));
            return true;
        } catch (IOException exception) {
            log.error(
                    "Failed to deliver message to WebSocket session {} for user {}",
                    getConnectionId(session),
                    recipientUserId,
                    exception
            );
            return false;
        }
    }

    private String serializePayload(final String userId, final Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize message payload for user {}", userId, exception);
            return null;
        }
    }

    private String getConnectionId(final WebSocketSession session) {
        return (String) session.getAttributes().get(CONNECTION_ID);
    }
}
