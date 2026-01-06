package io.poc.inventoryservice.service;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface InventoryService {

    List<FoodDto> getMenu();

    Mono<Boolean> reduceStock(List<OrderItemDto> foodList);

    Set<FoodDto> getStock(List<Long> foodIds);
}

