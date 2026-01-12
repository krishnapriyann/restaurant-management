package io.poc.paymentservice.service;

import io.poc.paymentservice.model.OrderDto;
import io.poc.paymentservice.model.PaymentDto;
import reactor.core.publisher.Mono;

public interface PaymentService {

    Mono<PaymentDto> pay(OrderDto order);
}
