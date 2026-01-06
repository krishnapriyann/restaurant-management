package io.poc.inventoryservice.service.impl;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import io.poc.inventoryservice.repository.InventoryRepository;
import io.poc.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
        log.info("Initializing InventoryServiceImpl");
    }

    @Override
    public List<FoodDto> getMenu() {
        log.info("Entering InventoryController::getMenu()");
        return inventoryRepository.getAllFood()
                .stream()
                .map(food -> {
                    return FoodDto.builder()
                            .id(food.getFoodId())
                            .name(food.getName())
                            .price(food.getPrice())
                            .description(food.getDescription())
                            .stock(food.getStock())
                            .build();
                        }
                )
                .peek(food -> log.info(food.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Boolean> reduceStock(List<OrderItemDto> order) {

        log.info("Entering InventoryController::reduceStock()");

        if(!order.isEmpty()) {
            log.info("Order size: {}", order.size());

            List<Food> reducedQuantity = order
                    .stream()
                    .map(orderItem -> {
                        Food food = inventoryRepository.getByFoodId(orderItem.getFoodId());
                        food.setStock(food.getStock() - orderItem.getQuantity());
                        log.info("Stock reduced for food: {} from: {} to {}", food.getName(), food.getStock(), food.getStock() - orderItem.getQuantity());
                        return food;
                    })
                    .collect(Collectors.toList());

            inventoryRepository.saveAll(reducedQuantity);
            log.info("InventoryController::reduceStock() successful");
            log.info("Exiting InventoryController::reduceStock()");

            return Mono.just(true)
                    .delayElement(Duration.ofSeconds(10));
        }
        log.info("Order is empty");

        log.info("InventoryController::reduceStock() failed");
        log.info("Exiting InventoryController::reduceStock()");
        return Mono.just(false)
                .delayElement(Duration.ofSeconds(10));
    }

    @Override
    public Set<FoodDto> getStock(List<Long> foodIds) {
        log.info("Entering InventoryController::getStock()");

        if(foodIds.isEmpty()) {
            log.info("Empty food id list.");
            throw new IllegalArgumentException("foodIds is empty");
        }

        log.info("Food ids found of size: {}", foodIds.size());
        return inventoryRepository.getFoodByIds(foodIds)
                .stream()
                .map(
                        food-> FoodDto.builder()
                                .name(food.getName())
                                .stock(food.getStock())
                .build())
                .peek(food -> log.info(food.toString()))
                .collect(Collectors.toSet());
    }

}
