package com.bloomreach.notification.service.model;

public record EmailNotificationPayload(String subject, String body) implements NotificationPayload {

}
