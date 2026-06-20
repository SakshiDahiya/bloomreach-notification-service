package com.bloomreach.integration.webapp;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.integration.common.LiveWebsocketsServerSupport;
import com.bloomreach.integration.common.WebSocketConnection;
import com.bloomreach.integration.common.WebSocketTestClient;
import com.bloomreach.notification.NotificationApplication;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        classes = NotificationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebappLiveDeliveryIntegrationTest extends LiveWebsocketsServerSupport {

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
    void deliversCreatedWebappNotificationOverLiveWebsocket() throws Exception {
        activeConnection = WebSocketTestClient.connect(websocketsPort(), "user-live-delivery");
        Thread.sleep(200);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Platform release",
                                  "message": "Version 2 is live.",
                                  "severity": "INFO",
                                  "notificationType": "WEBAPP",
                                  "audience": {
                                    "userIds": ["user-live-delivery"],
                                    "groups": [],
                                    "labels": {}
                                  },
                                  "webappPayload": {
                                    "title": "Platform release",
                                    "description": "Version 2 is live.",
                                    "action": "Open dashboard"
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", notNullValue()));

        final String websocketPayload = activeConnection.handler().awaitMessage(5, TimeUnit.SECONDS);
        assertNotNull(websocketPayload, "Expected a WebSocket payload after notification delivery");
        assertTrue(websocketPayload.contains("Platform release"));
    }
}
