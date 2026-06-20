package com.bloomreach.integration.history;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.integration.common.NotificationRequestSupport;
import com.bloomreach.integration.common.WebsocketServiceStubSupport;
import com.bloomreach.notification.NotificationApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = NotificationApplication.class)
@AutoConfigureMockMvc
class DeliveryStatusIntegrationTest extends WebsocketServiceStubSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updatesWebappDeliveryStatusFromSentToDeliveredToRead() throws Exception {
        final String notificationId = NotificationRequestSupport.createWebappNotification(
                mockMvc, objectMapper, "user-history-status", "Status update");

        mockMvc.perform(put("/api/notifications/{notificationId}/delivery-status", notificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-history-status",
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        mockMvc.perform(get("/api/notifications/pending")
                        .param("userId", "user-history-status")
                        .param("notificationType", "WEBAPP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(0));

        mockMvc.perform(put("/api/notifications/{notificationId}/delivery-status", notificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-history-status",
                                  "status": "READ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }

    @Test
    void rejectsDeliveryStatusUpdatesForEmailNotifications() throws Exception {
        final String notificationId = NotificationRequestSupport.createEmailNotification(
                mockMvc, objectMapper, "user-email-history");

        mockMvc.perform(put("/api/notifications/{notificationId}/delivery-status", notificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-email-history",
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
