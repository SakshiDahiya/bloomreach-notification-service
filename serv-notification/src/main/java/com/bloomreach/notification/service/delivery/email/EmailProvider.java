package com.bloomreach.notification.service.delivery.email;

public interface EmailProvider {

    void send(EmailRequest request);
}
