package io.poc.inventoryservice.service;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderDto;
import io.poc.inventoryservice.model.ReservationDto;
import io.poc.inventoryservice.model.ReservationResult;

import java.util.List;

public interface InventoryService {

    List<FoodDto> getMenu();

    ReservationResult reserve(OrderDto order);

    void confirm(Long orderId);

    void cancel(Long orderId);
}

