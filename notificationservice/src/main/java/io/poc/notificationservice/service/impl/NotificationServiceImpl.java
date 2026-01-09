package io.poc.notificationservice.service.impl;

import io.poc.notificationservice.model.OrderDto;
import io.poc.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    public Boolean notify(OrderDto order) {
        log.info("Entering NotificationServiceImpl::notify()");

        String email = order.getEmail();
        String status = order.getOrderStatus();
        log.info("email: {}, status: {}", email, status);

        if(status.equalsIgnoreCase("ORDER_PLACED")) {
            System.out.println("Payment for " + email + " done Successfully");
            return true;

        } else if(status.equalsIgnoreCase("ORDER_CANCELLED")) {
            System.out.println("Payment for " + email + " cancelled successfully");
            return true;
        }

        log.info("Exiting NotificationServiceImpl::notify()");
        return false;
    }
}
