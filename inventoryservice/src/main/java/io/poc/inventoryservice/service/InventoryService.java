package io.poc.inventoryservice.service;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;

import java.util.List;

public interface InventoryService {

    List<FoodDto> getMenu();

    void reserve(List<OrderItemDto> orderItems);

    void confirm(List<OrderItemDto> items);

    void cancel(List<OrderItemDto> items);
}

