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

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
        log.info("PaymentController initialized");
    }

    @PostMapping("/pay")
    public Mono<PaymentDto> payment(@RequestBody OrderDto order) {

        log.info("Payment request received for orderId={}", order.getOrderId());

        return paymentService.pay(order)
                .doOnSuccess(p ->
                        log.info("Payment processed for orderId={}, status={}",
                                order.getOrderId(), p.getStatus()))
                .doOnError(e ->
                        log.error("Payment failed for orderId={}", order.getOrderId(), e));
    }
}

