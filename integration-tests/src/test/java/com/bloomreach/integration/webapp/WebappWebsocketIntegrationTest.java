package com.bloomreach.integration.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.integration.common.WebSocketConnection;
import com.bloomreach.integration.common.WebSocketTestClient;
import com.bloomreach.websockets.WebSocketsApplication;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = WebSocketsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebappWebsocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private WebSocketConnection activeConnection;

    @AfterEach
    void tearDown() throws Exception {
        if (activeConnection != null && activeConnection.session().isOpen()) {
            activeConnection.session().close();
        }
        activeConnection = null;
    }

    @Test
    void pushesPayloadToConnectedClient() throws Exception {
        activeConnection = WebSocketTestClient.connect(port, "user-ws-connected");
        Thread.sleep(200);

        mockMvc.perform(post("/api/internal/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-ws-connected",
                                  "payload": {
                                    "title": "Maintenance",
                                    "description": "System unavailable tonight",
                                    "action": "View details"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.deliveredConnectionCount").value(1));

        final String payload = activeConnection.handler().awaitMessage(5, TimeUnit.SECONDS);
        assertTrue(payload.contains("Maintenance"));
        assertTrue(payload.contains("View details"));
    }

    @Test
    void reportsUserOfflineWhenNoActiveSessions() throws Exception {
        mockMvc.perform(post("/api/internal/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "offline-user",
                                  "payload": {
                                    "title": "Hello"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.deliveredConnectionCount").value(0));
    }

    @Test
    void deliversToAllActiveSessionsForSameUser() throws Exception {
        final WebSocketConnection firstConnection = WebSocketTestClient.connect(port, "user-ws-multi");
        final WebSocketConnection secondConnection = WebSocketTestClient.connect(port, "user-ws-multi");
        Thread.sleep(200);

        mockMvc.perform(post("/api/internal/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-ws-multi",
                                  "payload": {
                                    "title": "Broadcast"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveredConnectionCount").value(2));

        assertEquals("Broadcast", extractTitle(firstConnection.handler().awaitMessage(5, TimeUnit.SECONDS)));
        assertEquals("Broadcast", extractTitle(secondConnection.handler().awaitMessage(5, TimeUnit.SECONDS)));

        firstConnection.session().close();
        secondConnection.session().close();
    }

    @Test
    void rejectsHandshakeWithoutUserIdHeader() {
        assertFalse(connectWithoutUserIdSucceeded());
    }

    private boolean connectWithoutUserIdSucceeded() {
        try {
            final var session = WebSocketTestClient.connectWithoutUserId(port);
            session.close();
            return true;
        } catch (final Exception exception) {
            return false;
        }
    }

    private String extractTitle(final String jsonPayload) {
        return jsonPayload.replaceAll(".*\"title\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }
}
