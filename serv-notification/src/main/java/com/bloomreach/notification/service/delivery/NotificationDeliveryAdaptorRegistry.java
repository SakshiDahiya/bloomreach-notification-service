package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationDeliveryAdaptorRegistry {

    private final Map<NotificationType, NotificationDeliveryAdaptor> adaptors;

    public NotificationDeliveryAdaptorRegistry(final List<NotificationDeliveryAdaptor> adaptors) {
        this.adaptors = new EnumMap<>(NotificationType.class);
        for (final NotificationDeliveryAdaptor adaptor : adaptors) {
            this.adaptors.put(adaptor.supportedType(), adaptor);
        }
    }

    public NotificationDeliveryAdaptor getAdaptor(final NotificationType type) {
        final NotificationDeliveryAdaptor adaptor = adaptors.get(type);
        if (adaptor == null) {
            throw new IllegalArgumentException("No delivery adaptor registered for type: " + type);
        }
        return adaptor;
    }
}
