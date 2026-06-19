package com.bloomreach.webnotification.service;

import com.bloomreach.webnotification.model.WebAppRequest;

/**
 * Service for delivering notifications to connected web clients.
 */
public interface NotificationDeliveryService {

    /**
     * Delivers a web notification to the active WebSocket sessions of each recipient user.
     *
     * @param webAppRequest delivery request containing recipient user ids and notification content
     */
    void deliver(final WebAppRequest webAppRequest);
}
