package io.poc.orderservice.service.impl;

import io.poc.orderservice.entity.Order;
import io.poc.orderservice.entity.OrderItem;
import io.poc.orderservice.model.*;
import io.poc.orderservice.repository.OrderItemRepository;
import io.poc.orderservice.repository.OrderRepository;
import io.poc.orderservice.service.OrderService;
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

@Service
public class OrderServiceImpl implements OrderService {

    @Value("${service.inventory}")
    private String INVENTORY_SERVICE;

    @Value("${service.payment}")
    private String PAYMENT_SERVICE;

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
        return restTemplate.exchange(
                INVENTORY_SERVICE + "/menu",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<FoodDto>>() {}
        ).getBody();
    }

    public Mono<OrderDto> placeOrder(OrderDto order) {

        List<OrderItem> orderItemsList = buildOrderItems(order);

        order.setOrderStatus("CREATED");
        Order orderToPersist = setOrderStatus(order, "CREATED", orderItemsList);

//        Database calls are blocking - save 1.
        Order savedOrder = orderRepository.save(orderToPersist);


//        order ID is set to order DTO since the ID is auto gen.
        order.setOrderId(savedOrder.getOrderId());

        return reservation(order).flatMap(result -> {

            if (result.getReservationStatus().equalsIgnoreCase("RESERVED")) {

//                After reservation if reservation status is 'RESERVED' - save 2.
//                Order orderToReserve = setOrderStatus(order, "RESERVED", orderItemsList);
                savedOrder.setOrderStatus("RESERVED");
                Order reservedOrder = orderRepository.save(savedOrder);

                order.setOrderStatus(reservedOrder.getOrderStatus());
                Mono<PaymentDto> payment = pay(order);

                return payment.flatMap(pay -> {

                    if (!pay.getStatus().equalsIgnoreCase("PAYMENT_CANCELLED")) {

//                        Payment completed and order placed - save 3.
                        reservedOrder.setOrderStatus("COMPLETED");
                        Order completedOrder = orderRepository.save(reservedOrder);

                        return Mono.just(
                                OrderDto.builder()
                                        .items(buildOrderItemDtos(completedOrder.getItems()))
                                        .orderValue(completedOrder.getOrderValue())
                                        .orderStatus(completedOrder.getOrderStatus())
                                        .userId(completedOrder.getUserId())
                                        .email(completedOrder.getEmail())
                                        .orderId(completedOrder.getOrderId())
                                        .build()
                        );
                    }

//                    Payment failed and order cancelled
                    reservedOrder.setOrderStatus("CANCELLED");
                    Order cancelledOrder = orderRepository.save(reservedOrder);

                    return Mono.just(
                            OrderDto.builder()
                                    .orderId(cancelledOrder.getOrderId())
                                    .items(buildOrderItemDtos(cancelledOrder.getItems()))
                                    .orderValue(cancelledOrder.getOrderValue())
                                    .orderStatus(cancelledOrder.getOrderStatus())
                                    .userId(cancelledOrder.getUserId())
                                    .email(cancelledOrder.getEmail())
                                    .build()
                    );
                });
            }


            order.setOrderStatus("RESERVATION_FAILED");

//            Order not reserved and failed to be placed - save 6.
            Order failedOrder = setOrderStatus(order, "FAILED", orderItemsList);
            Order failed = orderRepository.save(failedOrder);


            return Mono.just(OrderDto.builder()
                    .orderId(failed.getOrderId())
                    .items(buildOrderItemDtos(failed.getItems()))
                    .userId(failed.getUserId())
                    .orderValue(failed.getOrderValue())
                    .orderStatus(failed.getOrderStatus())
                    .email(failed.getEmail())
                    .build()
            );
        });
    }

    private List<OrderItem> buildOrderItems(OrderDto orderRequest) {
        return orderRequest.getItems()
                .stream()
                .map(item -> OrderItem.builder()
                        .foodId(item.getFoodId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
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

    private Order setOrderStatus(OrderDto orderDto, String status, List<OrderItem> items) {

        return Order.builder()
                .items(items)
                .userId(orderDto.getUserId())
                .orderValue(orderDto.getOrderValue())
                .orderStatus(status)
                .email(orderDto.getEmail())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    Mono<ReservationResult> reservation(OrderDto orderDto) {
        return webClient.post()
                .uri(INVENTORY_SERVICE + "/reserve")
                .bodyValue(orderDto)
                .retrieve()
                .bodyToMono(ReservationResult.class);
    }

    private Mono<PaymentDto> pay(OrderDto order) {
        return webClient.post()
                .uri(PAYMENT_SERVICE + "/pay")
                .bodyValue(order)
                .exchangeToMono(response ->
                        response.bodyToMono(PaymentDto.class));
    }
}
