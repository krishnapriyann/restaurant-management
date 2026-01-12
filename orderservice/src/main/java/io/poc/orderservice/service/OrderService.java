package io.poc.orderservice.service;

import io.poc.orderservice.model.FoodDto;
import io.poc.orderservice.model.OrderDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {

    List<FoodDto> menu();

    Mono<OrderDto> placeOrder(OrderDto order);

    Long calculateOrderValue(Long orderId);
}
