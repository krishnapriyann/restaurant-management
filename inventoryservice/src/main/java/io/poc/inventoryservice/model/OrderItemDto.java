package io.poc.inventoryservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItemDto {
//    private Long id;
//    private Long orderId;
    private Long foodId;
    private int quantity;
    private Long price;
}
