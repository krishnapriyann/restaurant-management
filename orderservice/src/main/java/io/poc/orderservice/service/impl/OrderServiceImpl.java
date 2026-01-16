package io.poc.orderservice.service.impl;

import io.poc.orderservice.constants.OrderStatus;
import io.poc.orderservice.entity.Order;
import io.poc.orderservice.entity.OrderItem;
import io.poc.orderservice.exception.InventoryServiceException;
import io.poc.orderservice.exception.OrderProcessingException;
import io.poc.orderservice.exception.PaymentServiceException;
import io.poc.orderservice.model.*;
import io.poc.orderservice.repository.OrderItemRepository;
import io.poc.orderservice.repository.OrderRepository;
import io.poc.orderservice.service.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Value("${service.inventory}")
    private String INVENTORY_SERVICE;

    @Value("${service.payment}")
    private String PAYMENT_SERVICE;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            RestTemplate restTemplate,
            WebClient webClient,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.orderRepository = orderRepository;
    }


    @Override
    public List<FoodDto> menu() {
        log.info("Fetching menu from Inventory service");

        List<FoodDto> menu = restTemplate.exchange(
                INVENTORY_SERVICE + "/menu",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<FoodDto>>() {}
        ).getBody();

        if (menu == null) {
            log.error("Inventory service returned null menu");
            throw new InventoryServiceException("Menu response from inventory service is null");
        }

        log.info("Menu fetched successfully. Items count={}", menu.size());
        return menu;
    }

    @Override
    public Mono<OrderDto> placeOrder(OrderDto order) {

        log.info("Starting order placement for userId={}", order.getUserId());

        List<FoodDto> menu = menu();
        Map<Long, FoodDto> mappedMenu =
                menu.stream().collect(Collectors.toMap(FoodDto::getId, f -> f));

        List<OrderItem> orderItems = buildOrderItems(order, mappedMenu);

        Order createdOrder = persistOrder(order, OrderStatus.CREATED, orderItems);
        log.info("Order created with status={}, orderId={}",
                OrderStatus.CREATED, createdOrder.getOrderId());

        order.setOrderId(createdOrder.getOrderId());
        order.setOrderStatus(OrderStatus.CREATED);

        return reservation(order)
                .flatMap(reservationResult -> handleReservationResult(reservationResult, createdOrder, order))
                .onErrorMap(ex -> {
                    log.error("Order processing failed for orderId={}", order.getOrderId(), ex);
                    return new OrderProcessingException("Order processing failed", ex);
                });
    }


    private Mono<OrderDto> handleReservationResult(
            ReservationResult result,
            Order savedOrder,
            OrderDto orderDto) {

        if (result == null) {
            log.error("Reservation response is null for orderId={}", savedOrder.getOrderId());
            return Mono.error(new InventoryServiceException("Inventory service returned null"));
        }

        log.info("Reservation result received for orderId={}, status={}",
                savedOrder.getOrderId(), result.getReservationStatus());

        if (OrderStatus.RESERVED.equalsIgnoreCase(result.getReservationStatus())) {
            log.info("Inventory reserved successfully for orderId={}", savedOrder.getOrderId());
            return processPayment(savedOrder, orderDto);
        }

        log.warn("Reservation failed for orderId={}", savedOrder.getOrderId());
        savedOrder.setOrderStatus(OrderStatus.FAILED);
        Order failed = orderRepository.save(savedOrder);

        return Mono.just(buildOrderDto(failed));
    }

    private Mono<OrderDto> processPayment(Order reservedOrder, OrderDto orderDto) {

        reservedOrder.setOrderStatus(OrderStatus.RESERVED);
        Order savedReservedOrder = orderRepository.save(reservedOrder);
        orderDto.setOrderStatus(OrderStatus.RESERVED);

        log.info("Initiating payment for orderId={}", savedReservedOrder.getOrderId());

        return pay(orderDto)
                .flatMap(payment -> {

                    if (payment == null) {
                        log.error("Payment response is null for orderId={}", savedReservedOrder.getOrderId());
                        return Mono.error(new PaymentServiceException("Payment service returned null"));
                    }

                    if (!"PAYMENT_CANCELLED".equalsIgnoreCase(payment.getStatus())) {
                        log.info("Payment successful for orderId={}", savedReservedOrder.getOrderId());
                        savedReservedOrder.setOrderStatus(OrderStatus.COMPLETED);
                    } else {
                        log.info("Payment cancelled for orderId={}", savedReservedOrder.getOrderId());
                        savedReservedOrder.setOrderStatus(OrderStatus.CANCELLED);
                    }

                    Order finalOrder = orderRepository.save(savedReservedOrder);
                    log.info("Final order state persisted. orderId={}, status={}",
                            finalOrder.getOrderId(), finalOrder.getOrderStatus());

                    return Mono.just(buildOrderDto(finalOrder));
                });
    }


    private Order persistOrder(OrderDto orderDto, String status, List<OrderItem> items) {
        Order order = setOrderStatus(orderDto, status, items);
        return orderRepository.save(order);
    }

    private OrderDto buildOrderDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .items(buildOrderItemDtos(order.getItems()))
                .orderValue(order.getOrderValue())
                .orderStatus(order.getOrderStatus())
                .userId(order.getUserId())
                .email(order.getEmail())
                .build();
    }

    private List<OrderItem> buildOrderItems(OrderDto orderRequest, Map<Long, FoodDto> menu) {
        return orderRequest.getItems()
                .stream()
                .map(item -> {
                    FoodDto food = menu.get(item.getFoodId());
                    if (food == null) {
                        throw new InventoryServiceException("Invalid foodId: " + item.getFoodId());
                    }

                    return OrderItem.builder()
                            .foodId(item.getFoodId())
                            .quantity(item.getQuantity())
                            .price(food.getPrice())
                            .build();
                })
                .toList();
    }

    private List<OrderItemDto> buildOrderItemDtos(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> OrderItemDto.builder()
                        .foodId(item.getFoodId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();
    }

    private Long calculateTotal(List<OrderItem> items) {
        return items.stream()
                .mapToLong(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    private Order setOrderStatus(OrderDto orderDto, String status, List<OrderItem> items) {
        Long total = calculateTotal(items);

        return Order.builder()
                .items(items)
                .userId(orderDto.getUserId())
                .orderValue(total)
                .orderStatus(status)
                .email(orderDto.getEmail())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }


    Mono<ReservationResult> reservation(OrderDto orderDto) {
        log.info("Calling Inventory service for reservation. orderId={}", orderDto.getOrderId());

        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve")
                .bodyValue(orderDto)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Inventory error response: {}", body);
                                    return Mono.error(new InventoryServiceException(body));
                                });
                    }
                    return response.bodyToMono(ReservationResult.class);
                });
    }

    private Mono<PaymentDto> pay(OrderDto order) {
        log.info("Calling Payment service. orderId={}", order.getOrderId());

        return webClient.post()
                .uri(PAYMENT_SERVICE + "/pay")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .onErrorMap(ex -> {
                    log.error("Payment service call failed for orderId={}", order.getOrderId(), ex);
                    return new PaymentServiceException("Payment service unavailable");
                });
    }

}
