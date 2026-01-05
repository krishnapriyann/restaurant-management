package io.poc.inventoryservice.controller;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import io.poc.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-service")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/menu")
    public List<FoodDto> getMenu() {
        return inventoryService.getMenu();
    }

    @PutMapping("/reduce-stock")
    public Mono<Boolean> reduceStock(@RequestBody List<OrderItemDto> orderItems) {
        return inventoryService.reduceStock(orderItems);
    }
}
