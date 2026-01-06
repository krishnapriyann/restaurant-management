package io.poc.inventoryservice.service.impl;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.exception.OutOfStockException;
import io.poc.inventoryservice.model.FoodDto;
import io.poc.inventoryservice.model.OrderItemDto;
import io.poc.inventoryservice.repository.InventoryRepository;
import io.poc.inventoryservice.service.InventoryService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        log.info("Initializing InventoryServiceImpl");
    }

    @Override
    public List<FoodDto> getMenu() {
        log.info("Entering InventoryController::getMenu()");
        return inventoryRepository.getAllFood()
                .stream()
                .map(food -> FoodDto.builder()
                        .id(food.getFoodId())
                        .name(food.getName())
                        .price(food.getPrice())
                        .description(food.getDescription())
                        .stock(food.getStock())
                        .build()
                )
                .peek(food -> log.info(food.toString()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void reserve(List<OrderItemDto> items) {

        log.info("Entering InventoryController::reserve()");
        for (OrderItemDto item : items) {

            log.info("Setting reservation for food: {}", item.getFoodId());
            Food food = inventoryRepository.lockFoodById(item.getFoodId());

            if (food.getStock() < item.getQuantity()) {
                throw new OutOfStockException("Out of stock");
            }

            food.setStock(food.getStock() - item.getQuantity());
        }
        log.info("Exiting InventoryController::reserve()");
    }

    @Override
    @Transactional
    public void confirm(List<OrderItemDto> items) {
        log.info("Entering InventoryController::confirm()");

        for (OrderItemDto item : items) {
            log.info("OrderItemDto: {}", item);
            Food food = inventoryRepository.lockFoodById(item.getFoodId());

            log.info("Reservation confirmation: {}", food);
            food.setReservedCount(food.getReservedCount() - item.getQuantity());
            food.setStock(food.getStock() - item.getQuantity());
        }
        log.info("Exiting InventoryController::confirm()");
    }

    @Override
    @Transactional
    public void cancel(List<OrderItemDto> items) {
    log.info("Entering InventoryController::cancel()");
        for (OrderItemDto item : items) {

            Food food = inventoryRepository.lockFoodById(item.getFoodId());

            log.info("Resetting reservation due to failure");
            food.setReservedCount(food.getReservedCount() - item.getQuantity());
        }
        log.info("Exiting InventoryController::cancel()");
    }

}

