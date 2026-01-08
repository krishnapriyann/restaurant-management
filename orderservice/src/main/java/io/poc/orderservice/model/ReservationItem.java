package io.poc.orderservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReservationItem {
    private Long foodId;
    private int quantity;

}
