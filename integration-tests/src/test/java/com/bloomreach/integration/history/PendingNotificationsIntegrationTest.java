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
class PendingNotificationsIntegrationTest extends WebsocketServiceStubSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsSentWebappNotificationsForUser() throws Exception {
        final String firstNotificationId = NotificationRequestSupport.createWebappNotification(
                mockMvc, objectMapper, "user-pending-sent", "First update");
        final String secondNotificationId = NotificationRequestSupport.createWebappNotification(
                mockMvc, objectMapper, "user-pending-sent", "Second update");

        mockMvc.perform(get("/api/notifications/pending")
                        .param("userId", "user-pending-sent")
                        .param("notificationType", "WEBAPP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(2))
                .andExpect(jsonPath("$.notifications[0].id").value(firstNotificationId))
                .andExpect(jsonPath("$.notifications[1].id").value(secondNotificationId))
                .andExpect(jsonPath("$.notifications[0].status").value("SENT"))
                .andExpect(jsonPath("$.notifications[1].status").value("SENT"));
    }

    @Test
    void excludesDeliveredNotificationsFromPendingResults() throws Exception {
        final String sentNotificationId = NotificationRequestSupport.createWebappNotification(
                mockMvc, objectMapper, "user-pending-filter", "Still pending");
        final String deliveredNotificationId = NotificationRequestSupport.createWebappNotification(
                mockMvc, objectMapper, "user-pending-filter", "Already delivered");

        mockMvc.perform(put("/api/notifications/{notificationId}/delivery-status", deliveredNotificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-pending-filter",
                                  "status": "DELIVERED"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/pending")
                        .param("userId", "user-pending-filter")
                        .param("notificationType", "WEBAPP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.length()").value(1))
                .andExpect(jsonPath("$.notifications[0].id").value(sentNotificationId));
    }
}
