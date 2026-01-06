package io.poc.inventoryservice.exception.handler;

import io.poc.inventoryservice.exception.OutOfStockException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> outOfStockException(){
        return ResponseEntity.notFound().build();
    }
}
