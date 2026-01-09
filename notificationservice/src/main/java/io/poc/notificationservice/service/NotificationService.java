package io.poc.notificationservice.service;

import io.poc.notificationservice.model.OrderDto;

public interface NotificationService {

    Boolean notify(OrderDto order);
}
