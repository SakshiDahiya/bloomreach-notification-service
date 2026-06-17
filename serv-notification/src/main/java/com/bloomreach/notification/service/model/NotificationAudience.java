package com.bloomreach.notification.service.model;

import java.util.List;
import java.util.Map;

public record NotificationAudience(
        List<String> userIds,
        List<String> groups,
        Map<String, String> labels) {
}
