package com.bloomreach.integration.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public final class NotificationRequestSupport {

    private NotificationRequestSupport() {
    }

    public static String createEmailNotification(
            final MockMvc mockMvc,
            final ObjectMapper objectMapper,
            final String userId) throws Exception {
        final MvcResult result = mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Alert",
                                  "message": "Body",
                                  "severity": "INFO",
                                  "notificationType": "EMAIL",
                                  "audience": {
                                    "userIds": ["%s"],
                                    "groups": [],
                                    "labels": {}
                                  },
                                  "emailPayload": {
                                    "subject": "Alert",
                                    "body": "Body"
                                  }
                                }
                                """.formatted(userId)))
                .andExpect(status().isAccepted())
                .andReturn();

        return readNotificationId(objectMapper, result);
    }

    public static String createWebappNotification(
            final MockMvc mockMvc,
            final ObjectMapper objectMapper,
            final String userId,
            final String title) throws Exception {
        final MvcResult result = mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "message": "Description for %s",
                                  "severity": "WARNING",
                                  "notificationType": "WEBAPP",
                                  "audience": {
                                    "userIds": ["%s"],
                                    "groups": [],
                                    "labels": {}
                                  },
                                  "webappPayload": {
                                    "title": "%s",
                                    "description": "Description for %s",
                                    "action": "View details"
                                  }
                                }
                                """.formatted(title, title, userId, title, title)))
                .andExpect(status().isAccepted())
                .andReturn();

        return readNotificationId(objectMapper, result);
    }

    private static String readNotificationId(final ObjectMapper objectMapper, final MvcResult result)
            throws Exception {
        final JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asText();
    }
}
