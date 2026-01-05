package io.poc.notificationservice.service.impl;

import io.poc.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void notifyUser(String email) {
        logger.info("notifyUser");
        System.out.println("Payment for " + email + " done successfully");
    }
}
