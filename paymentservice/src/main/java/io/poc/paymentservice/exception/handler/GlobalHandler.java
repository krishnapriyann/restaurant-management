package io.poc.paymentservice.exception.handler;

import io.poc.paymentservice.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<String> handlePaymentProcessing(PaymentProcessingException ex) {
        log.error("Payment processing failed", ex);
        return ResponseEntity
                .status(503)   // Dependency failure or workflow failure
                .body("Payment service failed to process the request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Invalid payment request: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Unexpected error in Payment Service", ex);
        return ResponseEntity
                .status(500)
                .body("Unexpected server error");
    }
}
