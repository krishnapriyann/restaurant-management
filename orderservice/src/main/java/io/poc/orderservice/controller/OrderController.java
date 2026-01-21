package io.poc.orderservice.controller;

import io.poc.orderservice.exception.InvalidOrderException;
import io.poc.orderservice.model.FoodDto;
import io.poc.orderservice.model.OrderDto;
import io.poc.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-service")
public class OrderController {

    private final OrderService orderService;
    private final Logger log = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        log.info("OrderController initialized");
    }

    @GetMapping("/menu")
    public ResponseEntity<List<FoodDto>> getMenu() {
        log.info("Fetching menu");

        List<FoodDto> menu = orderService.menu();

        if (menu == null || menu.isEmpty()) {
            log.warn("Menu is empty");
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(menu);
    }

    @PostMapping("/order-confirmation")
    public Mono<OrderDto> placeOrder(
            @Valid @RequestBody OrderDto orderRequest) {
        log.info("Placing order request");

        if (orderRequest == null) {
            throw new InvalidOrderException("Order request cannot be null");
        }

        return orderService.placeOrder(orderRequest)
                .doOnSuccess(order -> log.info("Order placed successfully"))
                .doOnError(throwable -> log.error("Error while placing order", throwable));

    }
}
