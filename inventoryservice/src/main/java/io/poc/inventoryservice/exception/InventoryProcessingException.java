package io.poc.inventoryservice.exception;

public class InventoryProcessingException extends RuntimeException {
    public InventoryProcessingException(String message) {
        super(message);
    }
    public InventoryProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
