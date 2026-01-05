package io.poc.inventoryservice.service;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface InventoryService {

    List<FoodDto> getMenu();

    Mono<Boolean> reduceStock(List<OrderItemDto> foodList);
}

