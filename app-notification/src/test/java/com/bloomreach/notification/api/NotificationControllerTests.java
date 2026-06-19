package com.bloomreach.notification.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.notification.service.NotificationService;
import com.bloomreach.notification.service.model.NotificationResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTests.NotificationServiceTestConfiguration.class)
class NotificationControllerTests {

	private final MockMvc mockMvc;

	@Autowired
	NotificationControllerTests(final MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void createsNotification() throws Exception {
		mockMvc.perform(post("/api/notifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Scheduled maintenance",
								  "message": "The system will be unavailable tonight from 11 PM to 12 AM.",
								  "severity": "INFO",
								  "notificationType": "EMAIL",
								  "audience": {
								    "userIds": ["user-1"],
								    "groups": ["admins"],
								    "labels": {
								      "region": "us"
								    }
								  },
								  "emailPayload": {
								    "subject": "Scheduled maintenance",
								    "body": "The system will be unavailable tonight from 11 PM to 12 AM."
								  }
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.id", notNullValue()));
	}

	@Test
	void createsWebappNotification() throws Exception {
		mockMvc.perform(post("/api/notifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Product update",
								  "message": "A new release is available.",
								  "severity": "WARNING",
								  "notificationType": "WEBAPP",
								  "audience": {
								    "userIds": ["user-2"],
								    "groups": ["support"],
								    "labels": {
								      "region": "us"
								    }
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
	}

	@TestConfiguration
	static class NotificationServiceTestConfiguration {

		@Bean
		NotificationService notificationService() {
			return notification -> new NotificationResponse(UUID.randomUUID());
		}
	}
}
