package io.poc.inventoryservice.exception.handler;

import io.poc.inventoryservice.exception.InventoryProcessingException;
import io.poc.inventoryservice.exception.OutOfStockException;
import io.poc.inventoryservice.exception.ReservationNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<String> handleOutOfStock(OutOfStockException ex) {
        log.warn("Out of stock: {}", ex.getMessage());
        return ResponseEntity
                .status(409) // Conflict
                .body(ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<String> handleReservationNotFound(ReservationNotFoundException ex) {
        log.warn("Reservation not found: {}", ex.getMessage());
        return ResponseEntity
                .status(404)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InventoryProcessingException.class)
    public ResponseEntity<String> handleInventoryProcessing(InventoryProcessingException ex) {
        log.error("Inventory processing error", ex);
        return ResponseEntity
                .status(500)
                .body("Inventory service failed to process the request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(500)
                .body("Unexpected server error");
    }
}

