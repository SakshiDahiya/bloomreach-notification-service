package com.bloomreach.notification.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createsNotification() throws Exception {
		mockMvc.perform(post("/api/notifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Scheduled maintenance",
								  "message": "The system will be unavailable tonight from 11 PM to 12 AM.",
								  "severity": "INFO",
								  "delivery": "EMAIL",
								  "audience": {
								    "userIds": ["user-1"],
								    "groups": ["admins"],
								    "labels": {
								      "region": "us"
								    }
								  }
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.title").value("Scheduled maintenance"))
				.andExpect(jsonPath("$.severity").value("INFO"))
				.andExpect(jsonPath("$.delivery").value("EMAIL"));
	}
}
