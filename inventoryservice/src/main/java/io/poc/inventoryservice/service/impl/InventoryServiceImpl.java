package io.poc.inventoryservice.service.impl;

import io.poc.inventoryservice.constants.ReservationStatus;
import io.poc.inventoryservice.entity.Food;
import io.poc.inventoryservice.entity.Reservation;
import io.poc.inventoryservice.exception.InventoryProcessingException;
import io.poc.inventoryservice.exception.OutOfStockException;
import io.poc.inventoryservice.exception.ReservationNotFoundException;
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

    @Transactional
    @Override
    public Mono<ReservationResult> reserve(OrderDto order) {

        log.info("Starting reservation for orderId={}", order.getOrderId());

        try {
            List<OrderItemDto> items = order.getItems();
            items.sort(Comparator.comparing(OrderItemDto::getFoodId));

            List<ReservationItem> reservedItems = new ArrayList<>();

            for (OrderItemDto item : items) {

                Food food = inventoryRepository.lockFoodById(item.getFoodId());

                Optional<Reservation> existing =
                        reservationRepository.findByOrderIdAndFoodId(order.getOrderId(), food.getFoodId());

                if (existing.isPresent()) {
                    reservedItems.add(buildReservationItem(food, item));
                    continue;
                }

                int availableStock = food.getStock() - food.getReservedStock();

                if (availableStock < item.getQuantity()) {
                    log.warn("Out of stock. foodId={}, requested={}, available={}",
                            food.getFoodId(), item.getQuantity(), availableStock);
                    throw new OutOfStockException("Food " + food.getFoodId() + " is out of stock");
                }

                Reservation reservation = Reservation.builder()
                        .orderId(order.getOrderId())
                        .foodId(food.getFoodId())
                        .reservationCount(item.getQuantity())
                        .status(ReservationStatus.RESERVED)
                        .build();
                reservationRepository.save(reservation);

                food.setReservedStock(food.getReservedStock() + item.getQuantity());

                log.info("Reserved foodId={}, quantity={}", food.getFoodId(), item.getQuantity());

                reservedItems.add(buildReservationItem(food, item));
            }

            log.info("Reservation successful for orderId={}", order.getOrderId());

            return Mono.just(ReservationResult.builder()
                    .orderId(order.getOrderId())
                    .reservationItems(reservedItems)
                    .reservationStatus(ReservationStatus.RESERVED)
                    .build());

        } catch (Exception e) {
            log.error("Reservation failed for orderId={}", order.getOrderId(), e);
            throw new InventoryProcessingException("Reservation processing failed", e);
        }
    }

    @Transactional
    @Override
    public Mono<Void> confirm(Long orderId) {

        List<Reservation> reservations = reservationRepository.getByOrderId(orderId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found for orderId=" + orderId);
        }

        log.info("Confirming reservations for orderId={}, count={}", orderId, reservations.size());

        for (Reservation reservation : reservations) {

            if (ReservationStatus.CONFIRMED.equals(reservation.getStatus())
                    || ReservationStatus.CANCELLED.equals(reservation.getStatus())) {
                continue;
            }

            Food food = inventoryRepository.lockFoodById(reservation.getFoodId());
            food.setStock(food.getStock() - reservation.getReservationCount());

            reservation.setStatus(ReservationStatus.CONFIRMED);

            log.info("Confirmed reservation. foodId={}, remainingStock={}",
                    food.getFoodId(), food.getStock());
        }

        return Mono.empty();
    }

    @Transactional
    @Override
    public Mono<Void> cancel(Long orderId) {

        List<Reservation> reservations = reservationRepository.getByOrderId(orderId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException("No reservations found for orderId=" + orderId);
        }

        log.info("Cancelling reservations for orderId={}, count={}", orderId, reservations.size());

        for (Reservation reservation : reservations) {

            if (!ReservationStatus.RESERVED.equals(reservation.getStatus())) {
                continue;
            }

            Food food = inventoryRepository.lockFoodById(reservation.getFoodId());
            food.setReservedStock(food.getReservedStock() - reservation.getReservationCount());

            reservation.setStatus(ReservationStatus.CANCELLED);

            log.info("Cancelled reservation. foodId={}, released={}",
                    food.getFoodId(), reservation.getReservationCount());
        }

        return Mono.empty();
    }

    private ReservationItem buildReservationItem(Food food, OrderItemDto item) {
        return ReservationItem.builder()
                .foodId(food.getFoodId())
                .quantity(item.getQuantity())
                .build();
    }


}

