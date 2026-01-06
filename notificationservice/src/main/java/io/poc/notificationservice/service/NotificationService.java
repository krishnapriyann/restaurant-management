package io.poc.notificationservice.service;

public interface NotificationService {

    Boolean notifyUser(String email, String paymentStatus);
}
