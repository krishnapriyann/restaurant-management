package io.poc.orderservice.controller;

import io.poc.orderservice.model.FoodDto;
import io.poc.orderservice.model.OrderDto;
import io.poc.orderservice.service.OrderService;
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

    private OrderController(OrderService orderService) {
        this.orderService = orderService;
        log.info("Initializing OrderController");
    }

    @GetMapping(path = "/menu")
    public ResponseEntity<List<FoodDto>> getMenu(){
        log.info("Entering OrderController::getMenu");

        List<FoodDto> menu = orderService.menu();
        log.info("Menu fetched successfully");

        log.info("Exiting OrderController::getMenu");
        return ResponseEntity.ok().body(menu);
    }

    @PostMapping(path = "/order-confirmation")
    public ResponseEntity<Mono<OrderDto>> placeOrder(
            @RequestBody OrderDto orderRequest
    ){
        log.info("Entering OrderController::placeOrder");
        log.info("Placing order request for order: {}", orderRequest);

        Mono<OrderDto> order = orderService.placeOrder(orderRequest);

        log.info("Exiting OrderController::placeOrder");
        return ResponseEntity.ok(order);
    }
}
