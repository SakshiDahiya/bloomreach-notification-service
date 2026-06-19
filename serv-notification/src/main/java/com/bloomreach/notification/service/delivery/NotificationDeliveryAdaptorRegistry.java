package com.bloomreach.notification.service.delivery;

import com.bloomreach.notification.service.model.NotificationType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Registry that maps each {@link NotificationType} to its corresponding
 * {@link NotificationDeliveryAdaptor}.
 *
 * <p>All {@link NotificationDeliveryAdaptor} beans discovered by Spring are indexed
 * at construction time by {@link NotificationDeliveryAdaptor#supportedType()}.
 */
@Component
public class NotificationDeliveryAdaptorRegistry {

    private final Map<NotificationType, NotificationDeliveryAdaptor> adaptors;

    /**
     * Builds the registry from the supplied adaptors.
     *
     * @param adaptors delivery adaptors to register, typically all Spring-managed
     *                 {@link NotificationDeliveryAdaptor} implementations
     */
    public NotificationDeliveryAdaptorRegistry(final List<NotificationDeliveryAdaptor> adaptors) {
        this.adaptors = new EnumMap<>(NotificationType.class);
        for (final NotificationDeliveryAdaptor adaptor : adaptors) {
            this.adaptors.put(adaptor.supportedType(), adaptor);
        }
    }

    /**
     * Returns the delivery adaptor registered for the given notification type.
     *
     * @param type notification type to look up
     * @return the adaptor that handles deliveries for {@code type}
     * @throws IllegalArgumentException if no adaptor is registered for {@code type}
     */
    public NotificationDeliveryAdaptor getAdaptor(final NotificationType type) {
        final NotificationDeliveryAdaptor adaptor = adaptors.get(type);
        if (adaptor == null) {
            throw new IllegalArgumentException("No delivery adaptor registered for type: " + type);
        }
        return adaptor;
    }
}
