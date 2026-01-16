package io.poc.inventoryservice.service.impl;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.model.*;
import io.poc.inventoryservice.repository.InventoryRepository;
import io.poc.inventoryservice.service.InventoryService;
import io.poc.inventoryservice.service.ReservationTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationTransactionService txService;

    private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            ReservationTransactionService txService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.txService = txService;
        log.info("Initializing InventoryServiceImpl");
    }

    @Override
    public List<FoodDto> getMenu() {
        List<Food> foods = inventoryRepository.getAllFood();

        log.info("Menu requested. Items count={}", foods.size());

        return foods.stream()
                .map(food -> FoodDto.builder()
                        .id(food.getFoodId())
                        .name(food.getName())
                        .price(food.getPrice())
                        .description(food.getDescription())
                        .stock(food.getStock())
                        .build())
                .toList();
    }

    @Override
    public Mono<ReservationResult> reserve(OrderDto order) {
        return Mono.fromCallable(() -> txService.doReservation(order))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> confirm(Long orderId) {
        return Mono.fromRunnable(() -> txService.doConfirm(orderId))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> cancel(Long orderId) {
        return Mono.fromRunnable(() -> txService.doCancel(orderId))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

}

