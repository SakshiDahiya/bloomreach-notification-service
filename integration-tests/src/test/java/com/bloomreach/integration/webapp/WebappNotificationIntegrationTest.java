package com.bloomreach.integration.webapp;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.integration.common.WebsocketServiceStubSupport;
import com.bloomreach.notification.NotificationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = NotificationApplication.class)
@AutoConfigureMockMvc
class WebappNotificationIntegrationTest extends WebsocketServiceStubSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsWebappNotificationAndDelegatesDeliveryToWebsocketService() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Product update",
                                  "message": "A new release is available.",
                                  "severity": "WARNING",
                                  "notificationType": "WEBAPP",
                                  "audience": {
                                    "userIds": ["user-webapp-create"],
                                    "groups": [],
                                    "labels": {}
                                  },
                                  "webappPayload": {
                                    "title": "Product update",
                                    "description": "A new release is available.",
                                    "action": "View release notes"
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", notNullValue()));

        wireMockServer().verify(postRequestedFor(urlEqualTo("/api/internal/messages"))
                .withRequestBody(matchingJsonPath("$.userId", equalTo("user-webapp-create")))
                .withRequestBody(matchingJsonPath("$.payload.title", equalTo("Product update"))));
    }
}
