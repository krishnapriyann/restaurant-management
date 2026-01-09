package io.poc.inventoryservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReservationResult {
    private Long orderId;
    private List<ReservationItem> reservationItems;
    private String reservationStatus;
}
