package io.poc.paymentservice.service.impl;

import io.poc.paymentservice.constants.PaymentType;
import io.poc.paymentservice.entity.Payment;
import io.poc.paymentservice.model.OrderDto;
import io.poc.paymentservice.model.PaymentDto;
import io.poc.paymentservice.proxy.NotificationProxy;
import io.poc.paymentservice.repository.PaymentRepository;
import io.poc.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final NotificationProxy notificationProxy;
    private final WebClient webClient;

    @Value("${service.inventory}")
    private String INVENTORY_SERVICE;


    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            NotificationProxy notificationProxy,
            WebClient webClient) {

        this.paymentRepository = paymentRepository;
        this.notificationProxy = notificationProxy;
        this.webClient = webClient;
        log.info("Initializing PaymentServiceImpl");
    }

    @Override
    public Mono<PaymentDto> pay(OrderDto order) {
        log.info("Entering PaymentServiceImpl::makePayment");
        log.info("Processing payment for OrderID={} | Amount={} | Email={}",
                order.getOrderId(), order.getOrderValue(), order.getEmail());

        String orderStatus = order.getOrderStatus();

        if (orderStatus.equalsIgnoreCase("RESERVED")) {

            Payment payment = Payment.builder()
                    .orderId(order.getOrderId())
                    .amount(order.getOrderValue())
                    .paymentType(PaymentType.UPI.name())
                    .status("COMPLETE")
                    .build();

            log.info("Persisting Payment entity...");
            Payment persistedPayment = paymentRepository.save(payment);

            if (persistedPayment.getStatus().equalsIgnoreCase("COMPLETE")) {

                log.info("Payment persisted with ID={} for OrderID={}",
                        persistedPayment.getPaymentId(), persistedPayment.getOrderId());

                log.info("Triggering notification to {}",
                        order.getEmail());

                order.setOrderStatus("ORDER_PLACED");
                notificationProxy.notifyUser(order);

                log.info("Exiting PaymentServiceImpl::makePayment");

                return confirm(persistedPayment.getOrderId())
                        .thenReturn(PaymentDto.builder()
                                .amount(persistedPayment.getAmount())
                                .paymentType(persistedPayment.getPaymentType())
                                .status("PAYMENT_COMPLETE")
                                .build())
                        .delayElement(Duration.ofSeconds(10))
                        .doOnSuccess(o -> {

                            log.info("Payment COMPLETE Amount={}",
                                    persistedPayment.getAmount());

                            persistedPayment.setStatus("PAYMENT_COMPLETE");
                            paymentRepository.save(persistedPayment);

                        });
            }

            return cancel(order.getOrderId())
                    .thenReturn(PaymentDto.builder()
                            .amount(order.getOrderValue())
                            .status("CANCELLED")
                            .build())
                    .delayElement(Duration.ofSeconds(10))
                    .doOnSuccess(o -> {
                        log.info("Payment CANCELLED Amount={}", persistedPayment);

                        persistedPayment.setStatus("PAYMENT_CANCELLED");
                        paymentRepository.save(persistedPayment);
                    });
        }

        return Mono.just(PaymentDto.builder()
                .orderId(order.getOrderId())
                .amount(order.getOrderValue())
                .status("ORDER_CREATION_FAILED")
                .build());
    }

    private Mono<Void> confirm(Long orderId) {
        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve/confirm")
                .bodyValue(orderId)
                .exchangeToMono(
                        response -> response.bodyToMono(Void.class)
                )
                .doOnSubscribe(subscription -> log.info("Confirmation call sent successfully"));
    }

    private Mono<Void> cancel(Long orderId) {
        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve/cancel")
                .bodyValue(orderId)
                .exchangeToMono(
                        response -> response.bodyToMono(Void.class)
                )
                .doOnSubscribe(subscription -> log.info("Cancellation call sent successfully"));
    }
}
