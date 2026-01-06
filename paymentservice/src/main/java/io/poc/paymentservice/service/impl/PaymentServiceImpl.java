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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final NotificationProxy notificationProxy;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            NotificationProxy notificationProxy) {

        this.paymentRepository = paymentRepository;
        this.notificationProxy = notificationProxy;
        log.info("Initializing PaymentServiceImpl");
    }

    @Override
    public Mono<PaymentDto> makePayment(OrderDto order) {
        log.info("Entering PaymentServiceImpl::makePayment");
        log.info("Processing payment for OrderID={} | Amount={} | Email={}",
                order.getOrderId(), order.getOrderValue(), order.getEmail());

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .amount(order.getOrderValue())
                .paymentType(PaymentType.UPI.name())
                .status("COMPLETE")
                .build();

        log.info("Persisting Payment entity...");
        paymentRepository.save(payment);

        log.info("Payment persisted with ID={} for OrderID={}",
                payment.getPaymentId(), payment.getOrderId());

        log.info("Triggering notification to {}",
                    order.getEmail());

        notificationProxy.notifyUser(order.getEmail(), payment.getStatus());
        log.info("Notification triggered successfully");

        log.info("Payment COMPLETE Amount={}",
                payment.getAmount());

        log.info("Exiting PaymentServiceImpl::makePayment");
        return Mono.just(PaymentDto.builder()
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .status("COMPLETE")
                .build())
                .delayElement(Duration.ofSeconds(10));
    }
}
