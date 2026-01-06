package io.poc.notificationservice.service.impl;

import io.poc.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public Boolean notifyUser(String email, String  paymentStatus) {
        log.info("Entering NotificationServiceImpl::notifyUser");

        if(paymentStatus.equalsIgnoreCase("COMPLETE")) {
            System.out.println("Payment for " + email + " done Successfully");
            return true;
        }

        System.out.println("Payment for " + email + " Failed");
        log.info("Payment for {} Failed", email);
        log.info("Exiting NotificationServiceImpl::notifyUser");

        return false;
    }
}
