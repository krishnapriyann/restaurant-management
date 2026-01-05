package io.poc.paymentservice.controller;

import io.poc.paymentservice.model.OrderDto;
import io.poc.paymentservice.model.PaymentDto;
import io.poc.paymentservice.service.PaymentService;
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

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(path = "payment")
    public Mono<PaymentDto> makePayment(@RequestBody OrderDto order) {
        Mono<PaymentDto> paymentDto = paymentService.makePayment(order);
        return ResponseEntity.ok(paymentDto).getBody();
    }
}
