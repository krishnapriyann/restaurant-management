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
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public List<FoodDto> getMenu() {
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
                ).collect(Collectors.toList());
    }

    @Override
    public Mono<Boolean> reduceStock(List<OrderItemDto> order) {
        if(!order.isEmpty()) {
            List<Food> reducedQuantity = order.stream()
                    .map(orderItem -> {
                        Food food = inventoryRepository.getByFoodId(orderItem.getFoodId());
                        food.setStock(food.getStock() - orderItem.getQuantity());
                        return food;
                    }).collect(Collectors.toList());
            inventoryRepository.saveAll(reducedQuantity);
            return Mono.just(true)
                    .delayElement(Duration.ofSeconds(10));
        }
        return Mono.just(false)
                .delayElement(Duration.ofSeconds(10));
    }

}
