package io.poc.inventoryservice.controller;

import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import io.poc.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@RequestBody List<OrderItemDto> orderItems) {
        log.info("Entering InventoryController::reserve()");

        inventoryService.reserve(orderItems);
        log.info("Exiting InventoryController::reserve()");

        return ResponseEntity.ok().build();
    }
}
