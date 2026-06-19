package com.bloomreach.notification.service.delivery.webapp;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for {@code POST /api/internal/messages} on the app-websockets service.
 */
@Component
public class RestWebSocketMessageClient implements WebSocketMessageClient {

    private static final Logger log = LoggerFactory.getLogger(RestWebSocketMessageClient.class);
    private static final String SEND_MESSAGE_PATH = "/api/internal/messages";

    private final String baseUrl;
    private final RestClient restClient;

    public RestWebSocketMessageClient(
            final RestClient.Builder restClientBuilder,
            @Value("${websockets.service.base-url:http://localhost:8081}") final String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public SendMessageResponse sendMessage(final String userId, final Map<String, Object> payload) {
        final SendMessageRequest request = new SendMessageRequest(userId, payload);
        final SendMessageResponse response = restClient.post()
                .uri(SEND_MESSAGE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SendMessageResponse.class);

        log.info(
                "WebSocket message request sent [POST {}{}]: userId={}, connected={}, deliveredConnectionCount={}, connectionIds={}",
                baseUrl,
                SEND_MESSAGE_PATH,
                response.userId(),
                response.connected(),
                response.deliveredConnectionCount(),
                response.connectionIds()
        );
        return response;
    }
}
