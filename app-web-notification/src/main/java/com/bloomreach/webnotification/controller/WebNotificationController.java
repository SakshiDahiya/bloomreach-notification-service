package com.bloomreach.webnotification.controller;

import com.bloomreach.webnotification.service.NotificationDeliveryService;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for web notification operations.
 */
@RestController
public class WebNotificationController {

    private final NotificationDeliveryService notificationDeliveryService;

    /**
     * @param notificationDeliveryService service used to deliver notifications to connected clients
     */
    public WebNotificationController(final NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }
}
