package com.bloomreach.integration.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CollectingWebSocketHandler extends TextWebSocketHandler {

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) {
        messages.offer(message.getPayload());
    }

    public String awaitMessage(final long timeout, final TimeUnit unit) throws InterruptedException {
        return messages.poll(timeout, unit);
    }
}
