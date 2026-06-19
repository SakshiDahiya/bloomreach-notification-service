package com.bloomreach.websockets.registry;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.CONNECTION_ID;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Thread-safe registry of active {@link WebSocketSession WebSocket sessions} keyed by user id.
 *
 * <p>Each user may have multiple concurrent sessions. Session sets are backed by a
 * {@link java.util.concurrent.ConcurrentHashMap} and {@link ConcurrentHashMap#newKeySet()}.
 */
@Component
public class WebSocketSessionRegistry {

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    /**
     * Registers an active WebSocket session for the given user.
     *
     * @param userId  authenticated user id
     * @param session WebSocket session to register
     */
    public void register(final String userId, final WebSocketSession session) {
        sessions.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * Removes a WebSocket session for the given user.
     *
     * <p>If the user has no remaining sessions after removal, the user entry is dropped
     * from the registry.
     *
     * @param userId  authenticated user id
     * @param session WebSocket session to remove
     */
    public void remove(final String userId, final WebSocketSession session) {
        sessions.computeIfPresent(userId, (id, userSessions) -> {
            userSessions.remove(session);
            return userSessions.isEmpty() ? null : userSessions;
        });
    }

    /**
     * Returns an immutable snapshot of the active sessions for the given user.
     *
     * @param userId authenticated user id
     * @return active sessions for {@code userId}, or an empty set if none are registered
     */
    public Set<WebSocketSession> getSessions(final String userId) {
        final Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(userSessions);
    }

    /**
     * Returns the connection ids for the active sessions of the given user.
     *
     * @param userId authenticated user id
     * @return active connection ids for {@code userId}
     */
    public Set<String> getConnectionIds(final String userId) {
        return getSessions(userId).stream()
                .map(this::getConnectionId)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Checks whether the given user has at least one active WebSocket session.
     *
     * @param userId authenticated user id
     * @return {@code true} if the user has one or more registered sessions
     */
    public boolean isConnected(final String userId) {
        final Set<WebSocketSession> userSessions = sessions.get(userId);
        return userSessions != null && !userSessions.isEmpty();
    }

    private String getConnectionId(final WebSocketSession session) {
        return (String) session.getAttributes().get(CONNECTION_ID);
    }
}
