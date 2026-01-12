package io.poc.orderservice.exception.handler;

import io.poc.orderservice.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<String> handleInvalidOrder(InvalidOrderException ex) {
        log.warn("Invalid order request: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<String> handleInventoryFailure(InventoryServiceException ex) {
        log.error("Inventory service failure", ex);
        return ResponseEntity
                .status(503)   // Service Unavailable
                .body("Inventory service is currently unavailable");
    }

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<String> handlePaymentFailure(PaymentServiceException ex) {
        log.error("Payment service failure", ex);
        return ResponseEntity
                .status(503)   // Service Unavailable
                .body("Payment service is currently unavailable");
    }

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<String> handleOrderProcessingFailure(OrderProcessingException ex) {
        log.error("Order processing failed", ex);
        return ResponseEntity
                .status(500)
                .body("Order processing failed");
    }

    @ExceptionHandler(OrderTimeoutException.class)
    public ResponseEntity<String> handleOrderTimeoutFailure(OrderTimeoutException ex) {
        log.error("Order timeout failure", ex);
        return ResponseEntity
                .status(500)
                .body("Order timeout failure");
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<String> handlePaymentFailed(PaymentFailedException ex) {
        log.error("Payment failed", ex);
        return ResponseEntity
                .status(500)
                .body("Payment failed");
    }

    @ExceptionHandler(ReservationFailedException.class)
    public ResponseEntity<String> handleReservationFailed(ReservationFailedException ex) {
        log.error("Reservation failed", ex);
        return ResponseEntity
                .status(500)
                .body("Reservation failed");
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Unexpected error in Order Service", ex);
        return ResponseEntity
                .status(500)
                .body("Unexpected server error");
    }
}
