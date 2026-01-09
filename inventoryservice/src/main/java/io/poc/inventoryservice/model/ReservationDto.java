package io.poc.inventoryservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationDto {
    private Long reservationId;
    private Long orderId;
    private Long foodId;
    private int reservationCount;
    private String status;

}
