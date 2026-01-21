package io.poc.inventoryservice.service;

import io.poc.inventoryservice.model.OrderDto;
import io.poc.inventoryservice.model.ReservationResult;

public interface ReservationTransactionService {

    ReservationResult doReservation(OrderDto order);

    void doConfirm(Long orderId);

    void doCancel(Long orderId);

}