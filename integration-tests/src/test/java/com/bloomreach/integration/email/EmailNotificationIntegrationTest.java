package com.bloomreach.integration.email;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.notification.NotificationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = NotificationApplication.class)
@AutoConfigureMockMvc
class EmailNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsEmailNotificationThroughFullStack() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Scheduled maintenance",
                                  "message": "The system will be unavailable tonight.",
                                  "severity": "INFO",
                                  "notificationType": "EMAIL",
                                  "audience": {
                                    "userIds": ["user-email-create"],
                                    "groups": ["admins"],
                                    "labels": {}
                                  },
                                  "emailPayload": {
                                    "subject": "Scheduled maintenance",
                                    "body": "The system will be unavailable tonight."
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", notNullValue()));
    }
}
