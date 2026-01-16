package io.poc.paymentservice.service.impl;

import io.poc.paymentservice.constants.OrderStatus;
import io.poc.paymentservice.constants.PaymentStatus;
import io.poc.paymentservice.constants.PaymentType;
import io.poc.paymentservice.entity.Payment;
import io.poc.paymentservice.exception.PaymentProcessingException;
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
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

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
        log.info("PaymentService initialized");
    }

    @Override
    public Mono<PaymentDto> pay(OrderDto order) {

        log.info("Processing payment for orderId={}",
                order.getOrderId());

        if (!OrderStatus.RESERVED.equalsIgnoreCase(order.getOrderStatus())) {
            log.info("Order is not in RESERVED state. orderId={}, status={}",
                    order.getOrderId(), order.getOrderStatus());

            return Mono.just(PaymentDto.builder()
                    .orderId(order.getOrderId())
                    .amount(order.getOrderValue())
                    .status(PaymentStatus.ORDER_CREATION_FAILED)
                    .build());
        }

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .amount(order.getOrderValue())
                .paymentType(PaymentType.UPI.name())
                .status(PaymentStatus.COMPLETE)
                .build();

        Payment persistedPayment = paymentRepository.save(payment);

        log.info("Payment record created. paymentId={}, orderId={}",
                persistedPayment.getPaymentId(), persistedPayment.getOrderId());

        return confirm(persistedPayment.getOrderId())
                .then(Mono.defer(() -> handlePaymentSuccess(persistedPayment, order)))
                .onErrorResume(ex -> handlePaymentFailure(ex, persistedPayment, order));
    }

    private Mono<PaymentDto> handlePaymentSuccess(Payment payment, OrderDto order) {

        log.info("Payment successful. orderId={}", order.getOrderId());

        payment.setStatus(PaymentStatus.PAYMENT_COMPLETE);
        paymentRepository.save(payment);

        order.setOrderStatus(OrderStatus.ORDER_PLACED);

//        Since we are running a blocking call we need to use a blocking thread pool
//        not event loop
//        and fromRunnable is used because -> when subscribed run this action,
//        but do not emit a value.

        return Mono.fromRunnable(() -> notificationProxy.notifyUser(order))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(PaymentDto.builder()
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .paymentType(payment.getPaymentType())
                        .status(PaymentStatus.PAYMENT_COMPLETE)
                        .build())
                .delayElement(Duration.ofSeconds(1)));
    }

    private Mono<PaymentDto> handlePaymentFailure(Throwable ex, Payment payment, OrderDto order) {

        log.error("Confirmation failed for orderId={}", order.getOrderId(), ex);

        payment.setStatus(PaymentStatus.PAYMENT_CANCELLED);
        paymentRepository.save(payment);

        return cancel(order.getOrderId())
                .thenReturn(PaymentDto.builder()
                        .orderId(order.getOrderId())
                        .amount(order.getOrderValue())
                        .status(PaymentStatus.PAYMENT_CANCELLED)
                        .build())
                .delayElement(Duration.ofSeconds(10));
    }

    private Mono<Void> confirm(Long orderId) {
        log.info("Sending inventory confirmation for orderId={}", orderId);

        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve/confirm?orderId={orderId}", orderId)
                .bodyValue(orderId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(ex -> new PaymentProcessingException(
                        "Inventory confirmation failed for orderId=" + orderId, ex));
    }

    private Mono<Void> cancel(Long orderId) {
        log.info("Sending inventory cancellation for orderId={}", orderId);

        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve/cancel?orderId={orderId}", orderId)
                .bodyValue(orderId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(ex -> new PaymentProcessingException(
                        "Inventory cancellation failed for orderId=" + orderId, ex));
    }

}
