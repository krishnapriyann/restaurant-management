package io.poc.notificationservice.controller;

import io.poc.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-service")
public class NotificationController {

    private final NotificationService notificationService;
    private final Logger log = LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
        log.info("Initializing NotificationController");
    }

    @PostMapping("/notify")
    public ResponseEntity<Boolean> notifyUser(
            @RequestParam String email,
            @RequestParam String paymentStatus) {

        log.info("NotificationController::notifyUser");
        log.info("Email: {}", email);
        log.info("PaymentStatus: {}", paymentStatus);

        Boolean status = notificationService.notifyUser(email, paymentStatus);
        log.info("Exiting NotificationController::notifyUser");

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
