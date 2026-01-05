package io.poc.orderservice.controller;

import io.poc.orderservice.model.FoodDto;
import io.poc.orderservice.model.OrderDto;
import io.poc.orderservice.model.PaymentDto;
import io.poc.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-service")
public class OrderController {

    private final OrderService orderService;

    private OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(path = "/menu")
    public List<FoodDto> getMenu(){

        return orderService.menu();
    }

    @PostMapping(path = "/order-confirmation")
    public Mono<OrderDto> placeOrder(
            @RequestBody OrderDto orderRequest
    ){
        Mono<OrderDto> order = orderService.placeOrder(orderRequest);

        return ResponseEntity.ok(order).getBody();
    }
}
