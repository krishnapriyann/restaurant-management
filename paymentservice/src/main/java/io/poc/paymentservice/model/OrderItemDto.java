package io.poc.paymentservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItemDto {
    private Long foodId;
    private int quantity;
    private Long price;
}
