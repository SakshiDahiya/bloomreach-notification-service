package com.bloomreach.notification.service.model;

/**
 * Request to update a user's delivery status for a notification.
 */
public record UpdateDeliveryStatusRequest(
        String userId,
        DeliveryStatus status) {
}
