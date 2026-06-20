package com.bloomreach.integration.common;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class WebsocketServiceStubSupport {

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void overrideWebsocketsUrl(final DynamicPropertyRegistry registry) {
        ensureWireMockRunning();
        registry.add("websockets.service.base-url", WebsocketServiceStubSupport::wireMockBaseUrl);
    }

    @BeforeEach
    void resetWebsocketServiceStub() {
        ensureWireMockRunning();
        wireMockServer.resetAll();
        stubWebsocketDeliveryEndpoint();
    }

    protected static WireMockServer wireMockServer() {
        ensureWireMockRunning();
        return wireMockServer;
    }

    private static synchronized void ensureWireMockRunning() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            return;
        }

        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        stubWebsocketDeliveryEndpoint();
    }

    private static void stubWebsocketDeliveryEndpoint() {
        wireMockServer.stubFor(WireMock.post("/api/internal/messages")
                .willReturn(okJson("""
                        {
                          "userId": "stubbed-user",
                          "connected": true,
                          "deliveredConnectionCount": 1,
                          "connectionIds": ["connection-1"]
                        }
                        """)));
    }

    private static String wireMockBaseUrl() {
        return wireMockServer.baseUrl();
    }
}
