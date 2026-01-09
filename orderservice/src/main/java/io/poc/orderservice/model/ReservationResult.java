package io.poc.orderservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResult {
    private Long orderId;
    private List<ReservationItem> reservationItems;
    private String reservationStatus;

}
