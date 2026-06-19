package com.bloomreach.webnotification.model;

/**
 * Payload delivered to connected web clients.
 *
 * @param title       notification title
 * @param description notification description
 * @param action      call-to-action label or route
 */
public record WebAppNotificationPayload(String title, String description, String action) {
}
