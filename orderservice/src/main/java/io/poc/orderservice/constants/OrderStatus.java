package io.poc.orderservice.constants;

public enum OrderStatus {
    CREATED,
    STOCK_RESERVED,
    PAYMENT_FAILED,
    PAYMENT_SUCCESS,
    COMPLETED,
    OUT_OF_STOCK,
    INVENTORY_FAIL
}
