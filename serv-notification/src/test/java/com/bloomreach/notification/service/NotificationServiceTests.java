package com.bloomreach.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bloomreach.notification.service.model.Notification;
import org.junit.jupiter.api.Test;

class NotificationServiceTests {

	@Test
	void acceptsNotification() {
		NotificationService service = new NotificationService() {
			@Override
			public void sendNotification(Notification notification) {
			}
		};

		service.sendNotification(new Notification());

		assertThat(service).isNotNull();
	}
}
