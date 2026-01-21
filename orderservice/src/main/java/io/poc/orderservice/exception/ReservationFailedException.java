package io.poc.orderservice.exception;

public class ReservationFailedException extends RuntimeException {
    public ReservationFailedException(String message) {
        super(message);
    }
}
