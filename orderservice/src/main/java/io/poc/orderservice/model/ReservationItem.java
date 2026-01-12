package io.poc.orderservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationItem {
    private Long foodId;
    private int quantity;

}
