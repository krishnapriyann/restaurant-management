package io.poc.inventoryservice.service.impl;

import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.entity.Reservation;
import io.poc.inventoryservice.exception.OutOfStockException;
import io.poc.inventoryservice.model.*;
import io.poc.inventoryservice.repository.InventoryRepository;
import io.poc.inventoryservice.repository.ReservationRepository;
import io.poc.inventoryservice.service.InventoryService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            ReservationRepository reservationRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository =  reservationRepository;
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
    public Mono<ReservationResult> reserve(OrderDto order) {
        log.info("Entering InventoryController::reserve()");

        List<OrderItemDto> items = order.getItems();
        items.sort(Comparator.comparing(OrderItemDto::getFoodId));

        List<ReservationItem> reservedItems = new ArrayList<>();

        for (OrderItemDto item : items) {

            log.info("Setting reservation for food: {}", item.getFoodId());
            Food food = inventoryRepository.lockFoodById(item.getFoodId());

            Optional<Reservation> existing =
                    reservationRepository.findByOrderIdAndFoodId(order.getOrderId(), food.getFoodId());

            if(existing.isPresent()) {
                reservedItems.add(
                        ReservationItem.builder()
                                .foodId(food.getFoodId())
                                .quantity(item.getQuantity())
                                .build()
                );
                continue;
            }

            int availableStock = food.getStock() - food.getReservedStock();

            if (availableStock < item.getQuantity()) {
                log.info("The Food product has not enough stock");
                throw new OutOfStockException("Out of stock");
            }

            Reservation reservation = Reservation.builder()
                    .orderId(order.getOrderId())
                    .foodId(food.getFoodId())
                    .reservationCount(item.getQuantity())
                    .status("RESERVED")
                    .build();
                    reservationRepository.save(reservation);

            food.setReservedStock(food.getReservedStock() + item.getQuantity());

            reservedItems.add(ReservationItem.builder()
                    .foodId(food.getFoodId())
                    .quantity(item.getQuantity())
                    .build());
        }

        log.info("Exiting InventoryController::reserve()");
        return Mono.just(ReservationResult.builder()
                .orderId(order.getOrderId())
                .reservationItems(reservedItems)
                .reservationStatus("RESERVED")
                .build());

    }

    @Override
    @Transactional
    public Mono<Void> confirm(Long orderId) {
        log.info("Entering InventoryController::confirm()");

        List<Reservation> reservations = reservationRepository.getByOrderId(orderId);

        for(Reservation reservation : reservations) {

            if (reservation.getStatus().equals("CONFIRMED")) continue;
            if (reservation.getStatus().equals("CANCELLED")) continue;

            log.info("Confirming reservation...");
            Food food = inventoryRepository.lockFoodById(reservation.getFoodId());
            food.setStock(food.getStock() - reservation.getReservationCount());

            reservation.setStatus("CONFIRMED");
        }

        log.info("Exiting InventoryController::confirm()");
        return Mono.empty();
    }

    @Override
    @Transactional
    public Mono<Void> cancel(Long orderId) {
    log.info("Entering InventoryController::cancel()");

        List<Reservation> reservations = reservationRepository.getByOrderId(orderId);

        for(Reservation reservation : reservations) {

            if(!reservation.getStatus().equalsIgnoreCase("RESERVED")) continue;

            Food food = inventoryRepository.lockFoodById(reservation.getFoodId());
            food.setReservedStock(food.getReservedStock() - reservation.getReservationCount());

            reservation.setStatus("CANCELLED");

        }

        log.info("Exiting InventoryController::cancel()");
        return Mono.empty();
    }

}

