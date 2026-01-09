package io.poc.paymentservice.controller;

import io.poc.paymentservice.model.OrderDto;
import io.poc.paymentservice.model.PaymentDto;
import io.poc.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payment-service")
public class PaymentController {

    private final PaymentService paymentService;
    private final Logger log = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
        log.info("Initializing PaymentController");
    }

    @PostMapping(path = "pay")
    public ResponseEntity<Mono<PaymentDto>> payment(@RequestBody OrderDto order) {
        log.info("Entering PaymentController::makePayment");

        log.info("Order details: {}", order);

        Mono<PaymentDto> paymentDto = paymentService.pay(order);
        log.info("Exiting PaymentController::makePayment");

        return ResponseEntity.ok(paymentDto);
    }
}
