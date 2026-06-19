package com.bloomreach.websockets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the WebSockets application.
 *
 * <p>Hosts the WebSocket session endpoint used by frontend clients and the internal REST API
 * used by other services to push messages to connected sessions.
 */
@SpringBootApplication
public class WebSocketsApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebSocketsApplication.class, args);
	}
}
