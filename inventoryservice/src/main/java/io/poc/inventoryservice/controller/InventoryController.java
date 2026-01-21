package io.poc.inventoryservice.controller;

import io.poc.inventoryservice.model.*;
import io.poc.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-service")
public class InventoryController {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        log.info("InventoryController initialized");
    }

    @GetMapping("/menu")
    public ResponseEntity<List<FoodDto>> getMenu() {
        log.info("Fetching inventory menu");

        List<FoodDto> foodList = inventoryService.getMenu();

        if (foodList == null || foodList.isEmpty()) {
            log.warn("Menu is empty");
            return ResponseEntity.noContent().build();
        }

        log.info("Menu fetched successfully. Items count={}", foodList.size());
        return ResponseEntity.ok(foodList);
    }

    @PostMapping("/reserve")
    public Mono<ReservationResult> reserve(@RequestBody OrderDto order) {
        log.info("Reservation request received for orderId={}", order.getOrderId());
        return inventoryService.reserve(order)
                .doOnSuccess(r ->
                        log.info("Reservation completed for orderId={}, status={}",
                                order.getOrderId(), r.getReservationStatus()))
                .doOnError(e ->
                        log.error("Reservation failed for orderId={}", order.getOrderId(), e));
    }

    @PostMapping("/reserve/confirm")
    public Mono<Void> reserveConfirm(@RequestParam Long orderId) {
        log.info("Confirming reservation for orderId={}", orderId);

        return inventoryService.confirm(orderId)
                .doOnSuccess(v -> log.info("Reservation confirmed for orderId={}", orderId));
    }


    @PostMapping("/reserve/cancel")
    public Mono<Void> reserveCancel(@RequestParam Long orderId) {
        log.info("Cancelling reservation for orderId={}", orderId);

        return inventoryService.cancel(orderId).doOnSuccess(v -> log.info("Reservation cancelled for orderId={}", orderId));
    }
}