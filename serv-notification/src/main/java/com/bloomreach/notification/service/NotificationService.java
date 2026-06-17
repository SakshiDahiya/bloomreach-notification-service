package com.bloomreach.notification.service;

import com.bloomreach.notification.service.model.Notification;
import com.bloomreach.notification.service.model.NotificationResponse;

public interface NotificationService {

	NotificationResponse sendNotification(Notification notification);

}
