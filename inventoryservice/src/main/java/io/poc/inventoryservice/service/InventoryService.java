package io.poc.inventoryservice.service;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderDto;
import io.poc.inventoryservice.model.ReservationDto;
import io.poc.inventoryservice.model.ReservationResult;
import reactor.core.publisher.Mono;

import java.util.List;

public interface InventoryService {

    List<FoodDto> getMenu();

    Mono<ReservationResult> reserve(OrderDto order);

    Mono<Void> confirm(Long orderId);

    Mono<Void> cancel(Long orderId);
}

