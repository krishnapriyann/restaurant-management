package io.poc.paymentservice.constants;

public final class PaymentStatus {
    public static final String COMPLETE = "COMPLETE";
    public static final String PAYMENT_COMPLETE = "PAYMENT_COMPLETE";
    public static final String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";
    public static final String CANCELLED = "CANCELLED";
    public static final String ORDER_CREATION_FAILED = "ORDER_CREATION_FAILED";

    private PaymentStatus() {}
}

