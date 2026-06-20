package com.bloomreach.notification.service.delivery.email;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailProvider.class);
    private static final String SEND_GRID_MAIL_SEND_URL = "https://api.sendgrid.com/v3/mail/send";

    private final String fromEmail;
    private final String apiKey;

    public SendGridEmailProvider(
            @Value("${notification.email.sendgrid.from:noreply@example.com}") final String fromEmail,
            @Value("${notification.email.sendgrid.api-key:}") final String apiKey) {
        this.fromEmail = fromEmail;
        this.apiKey = apiKey;
    }

    @Override
    public void send(final EmailRequest request) {
        final Map<String, Object> requestBody = buildSendGridRequestBody(request);
        final Map<String, String> requestHeaders = Map.of(
                "Authorization", "Bearer " + apiKey,
                "Content-Type", "application/json"
        );

        log.info(
                "SendGrid email request prepared [POST {}]: headers={}, body={}",
                SEND_GRID_MAIL_SEND_URL,
                requestHeaders,
                requestBody
        );
    }

    private Map<String, Object> buildSendGridRequestBody(final EmailRequest request) {
        final List<Map<String, String>> to = request.recipients().stream()
                .map(email -> Map.of("email", email))
                .toList();

        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("personalizations", List.of(Map.of("to", to)));
        body.put("from", Map.of("email", fromEmail));
        body.put("subject", request.payload().subject());
        body.put("content", List.of(Map.of(
                "type", "text/plain",
                "value", request.payload().body()
        )));
        body.put("custom_args", Map.of("notification_id", request.notificationId().toString()));
        return body;
    }
}
