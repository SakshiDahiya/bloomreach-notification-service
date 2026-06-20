package com.bloomreach.integration.common;

import com.bloomreach.websockets.WebSocketsApplication;
import java.net.http.HttpClient;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

public abstract class LiveWebsocketsServerSupport {

    private static ConfigurableApplicationContext websocketsContext;
    private static boolean shutdownHookRegistered;

    @DynamicPropertySource
    static void registerWebsocketsBaseUrl(final DynamicPropertyRegistry registry) {
        ensureWebsocketsServerRunning();
        registry.add("websockets.service.base-url", LiveWebsocketsServerSupport::websocketsBaseUrl);
    }

    protected static int websocketsPort() {
        ensureWebsocketsServerRunning();
        return websocketsContext.getEnvironment()
                .getProperty("local.server.port", Integer.class);
    }

    private static synchronized void ensureWebsocketsServerRunning() {
        if (websocketsContext != null) {
            return;
        }

        websocketsContext = new SpringApplication(WebSocketsApplication.class)
                .run("--server.port=0");
        registerShutdownHook();
        waitUntilReady(websocketsBaseUrl());
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        shutdownHookRegistered = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (websocketsContext != null) {
                SpringApplication.exit(websocketsContext);
                websocketsContext = null;
            }
        }));
    }

    private static String websocketsBaseUrl() {
        return "http://localhost:" + websocketsContext.getEnvironment()
                .getProperty("local.server.port", Integer.class);
    }

    private static void waitUntilReady(final String baseUrl) {
        final RestClient client = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build()))
                .build();
        final long deadline = System.currentTimeMillis() + 10_000;

        while (System.currentTimeMillis() < deadline) {
            try {
                client.post()
                        .uri(baseUrl + "/api/internal/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("userId", "readiness-probe", "payload", Map.of("title", "probe")))
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (final Exception ignored) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for websockets server", interruptedException);
                }
            }
        }

        throw new IllegalStateException("Websockets server did not become ready at " + baseUrl);
    }
}
