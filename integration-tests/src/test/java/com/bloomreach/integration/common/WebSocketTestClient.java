package com.bloomreach.integration.common;

import static com.bloomreach.websockets.websocket.WebSocketSessionAttributes.USER_ID_HEADER;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public final class WebSocketTestClient {

    private WebSocketTestClient() {
    }

    public static WebSocketConnection connect(final int port, final String userId) throws Exception {
        final StandardWebSocketClient client = new StandardWebSocketClient();
        final CollectingWebSocketHandler handler = new CollectingWebSocketHandler();
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(USER_ID_HEADER, userId);

        final WebSocketSession session = client.execute(
                        handler,
                        headers,
                        URI.create("ws://localhost:" + port + "/ws/sessions"))
                .get(5, TimeUnit.SECONDS);

        return new WebSocketConnection(session, handler);
    }

    public static WebSocketSession connectWithoutUserId(final int port) throws Exception {
        final StandardWebSocketClient client = new StandardWebSocketClient();
        return client.execute(
                        new CollectingWebSocketHandler(),
                        new WebSocketHttpHeaders(),
                        URI.create("ws://localhost:" + port + "/ws/sessions"))
                .get(5, TimeUnit.SECONDS);
    }
}
