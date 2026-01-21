package io.poc.orderservice.exception;

public class OrderTimeoutException extends RuntimeException {
    public OrderTimeoutException(String message, Throwable ex) {
        super(message, ex);
    }
}
