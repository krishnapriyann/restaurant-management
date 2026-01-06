package io.poc.inventoryservice.controller;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import io.poc.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/inventory-service")
public class InventoryController {

    private final InventoryService inventoryService;
    private final Logger log = LoggerFactory.getLogger(InventoryController.class);

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        log.info("Initializing InventoryController");
    }

    @GetMapping("/menu")
    public ResponseEntity<List<FoodDto>> getMenu() {

        log.info("Entering InventoryController::getMenu()");
        List<FoodDto> foodList = inventoryService.getMenu();

        log.info("Exiting InventoryController::getMenu()");
        return ResponseEntity.ok().body(foodList);
    }

    @GetMapping("/stock")
    public ResponseEntity<Set<FoodDto>> getStock(@RequestBody List<Long> foodIds) {
        log.info("Entering InventoryController::getStock()");

        Set<FoodDto> stock = inventoryService.getStock(foodIds);

        log.info("Exiting InventoryController::getStock()");
        return ResponseEntity.ok(stock);
    }

    @PutMapping("/reduce-stock")
    public ResponseEntity<Mono<Boolean>> reduceStock(
            @RequestBody List<OrderItemDto> orderItems
    ) {
        log.info("Entering InventoryController::reduceStock()");
        Mono<Boolean> status = inventoryService.reduceStock(orderItems);

        log.info("Exiting InventoryController::reduceStock()");
        return ResponseEntity.ok().body(status);
    }
}
