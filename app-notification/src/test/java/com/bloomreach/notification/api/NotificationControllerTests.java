package com.bloomreach.notification.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bloomreach.notification.service.NotificationService;
import com.bloomreach.notification.service.model.NotificationSeverity;
import com.bloomreach.notification.service.model.NotificationType;
import com.bloomreach.notification.service.model.NotificationResponse;
import com.bloomreach.notification.service.model.NotificationUserDelivery;
import com.bloomreach.notification.service.model.PendingNotification;
import com.bloomreach.notification.service.model.UpdateDeliveryStatusRequest;
import com.bloomreach.notification.service.model.DeliveryStatus;
import com.bloomreach.notification.service.model.WebappNotificationPayload;
import java.time.Instant;
import java.util.List;
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

	@Test
	void updatesWebappDeliveryStatus() throws Exception {
		final UUID notificationId = UUID.fromString("11111111-1111-1111-1111-111111111111");

		mockMvc.perform(put("/api/notifications/{notificationId}/delivery-status", notificationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "userId": "user-2",
								  "status": "DELIVERED"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.notificationId").value(notificationId.toString()))
				.andExpect(jsonPath("$.userId").value("user-2"))
				.andExpect(jsonPath("$.status").value("DELIVERED"));
	}

	@Test
	void fetchesPendingWebappNotifications() throws Exception {
		final UUID notificationId = UUID.fromString("22222222-2222-2222-2222-222222222222");

		mockMvc.perform(get("/api/notifications/pending")
						.param("userId", "user-2")
						.param("notificationType", "WEBAPP"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.notifications.length()").value(1))
				.andExpect(jsonPath("$.notifications[0].id").value(notificationId.toString()))
				.andExpect(jsonPath("$.notifications[0].status").value("SENT"))
				.andExpect(jsonPath("$.notifications[0].webappPayload.title").value("Product update"));
	}

	@TestConfiguration
	static class NotificationServiceTestConfiguration {

		@Bean
		NotificationService notificationService() {
			return new NotificationService() {
				@Override
				public NotificationResponse sendNotification(final com.bloomreach.notification.service.model.Notification notification) {
					return new NotificationResponse(UUID.randomUUID());
				}

				@Override
				public NotificationUserDelivery updateDeliveryStatus(
						final UUID notificationId,
						final UpdateDeliveryStatusRequest request) {
					return new NotificationUserDelivery(
							notificationId,
							request.userId(),
							request.status(),
							Instant.parse("2026-06-20T00:00:00Z")
					);
				}

				@Override
				public List<PendingNotification> findPendingNotifications(
						final String userId,
						final NotificationType notificationType) {
					return List.of(new PendingNotification(
							UUID.fromString("22222222-2222-2222-2222-222222222222"),
							NotificationSeverity.WARNING,
							DeliveryStatus.SENT,
							Instant.parse("2026-06-20T00:00:00Z"),
							new WebappNotificationPayload("Product update", "A new release is available.", "View release notes")
					));
				}
			};
		}
	}
}
