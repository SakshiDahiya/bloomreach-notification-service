package com.bloomreach.webnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the web notification application.
 *
 * <p>Hosts the authenticated WebSocket endpoint used to deliver real-time
 * notifications to connected clients.
 */
@SpringBootApplication
public class WebNotificationApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebNotificationApplication.class, args);
	}
}
