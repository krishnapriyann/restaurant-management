package io.poc.orderservice.service.impl;

import io.poc.orderservice.entity.Order;
import io.poc.orderservice.entity.OrderItem;
import io.poc.orderservice.model.*;
import io.poc.orderservice.repository.OrderItemRepository;
import io.poc.orderservice.repository.OrderRepository;
import io.poc.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final String INVENTORY_SERVICE = "http://localhost:0001/api/v1/inventory-service";
    private final String PAYMENT_SERVICE = "http://localhost:0003/api/v1/payment-service";

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(
            RestTemplate restTemplate,
            WebClient webClient,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        log.info("OrderServiceImpl initialized");
    }

    @Override
    public List<FoodDto> menu() {
        log.info("OrderServiceImpl::menu");
        log.info("Fetching menu from Inventory Service");

        List<FoodDto> menu = restTemplate.exchange(
                INVENTORY_SERVICE + "/menu",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<FoodDto>>() {}
        ).getBody();

        log.info("Menu received. Count={}", menu != null ? menu.size() : 0);

        return menu;
    }

    @Override
    public Mono<OrderDto> placeOrder(OrderDto orderRequest) {

        log.info("Received order placement request: {}", orderRequest);

        // Build OrderItems
        List<OrderItem> orderItems = orderRequest.getItems()
                .stream()
                .map(item -> OrderItem.builder()
                        .foodId(item.getFoodId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        log.info("Mapped {} OrderItems", orderItems.size());

        // Build Order
        Order order = Order.builder()
                .items(orderItems)
                .userId(orderRequest.getUserId())
                .orderValue(orderRequest.getOrderValue())
                .orderStatus("CREATED")
                .email(orderRequest.getEmail())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        // Set back-reference
        orderItems.forEach(item -> item.setOrder(order));

        log.info("Persisting Order...");
        orderRepository.save(order);
        log.info("Order persisted with ID: {}", order.getOrderId());

        List<OrderItemDto> requestItems = orderRequest.getItems();

        if (!"CREATED".equalsIgnoreCase(order.getOrderStatus())) {
            log.warn("Order not in CREATED state. Returning early.");
            return Mono.just(map(order, requestItems));
        }

        log.info("Calling Payment Service for OrderID={}", order.getOrderId());

        Mono<PaymentDto> paymentDetail = webClient.post()
                .uri(PAYMENT_SERVICE + "/payment")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .doOnSubscribe(subscription -> log.info("Payment call sent successfully"))
                .doOnNext(response -> log.info("Payment response received: {}", response));

        return paymentDetail.flatMap(payment -> {

            log.info("Payment response received for OrderID={}: {}", order.getOrderId(), payment);
            log.info("Evaluating payment status: {}", payment.getStatus());
            if (!"COMPLETE".equalsIgnoreCase(payment.getStatus())) {

                log.error("Payment FAILED for OrderID={}", order.getOrderId());

                order.setOrderStatus("FAILED");
                order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                orderRepository.save(order);

                return Mono.just(map(order, requestItems));
            }

            log.info("Payment SUCCESS. Calling Inventory Service to reduce stock...");

            Mono<Boolean> stockStatus = webClient.put()
                    .uri(INVENTORY_SERVICE + "/reduce-stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestItems)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .doOnSubscribe(subscription -> log.info("inventory stock reduction call sent successfully"))
                    .doOnNext(response -> log.info("Inventory response received: {}", response));

            return stockStatus.flatMap(status -> {

                log.info("Inventory update result for OrderID={}: {}", order.getOrderId(), status);

                if (status) {
                    order.setOrderStatus("COMPLETED");
                } else {
                    order.setOrderStatus("INVENTORY FAIL");
                }
                log.info("Order status after inventory update: {}", order.getOrderStatus());

                order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                orderRepository.save(order);

                log.info("Order {} final status: {}", order.getOrderId(), order.getOrderStatus());

                return Mono.just(map(order, requestItems));
            });

        }).doOnError(e -> log.error("Order processing failed", e));
    }

    private OrderDto map(Order order, List<OrderItemDto> items) {
        return OrderDto.builder()
                .items(items)
                .userId(order.getUserId())
                .orderValue(order.getOrderValue())
                .orderStatus(order.getOrderStatus())
                .email(order.getEmail())
                .build();
    }
}
