package io.poc.paymentservice.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDto {
    private Long orderId;
    private List<OrderItemDto> items;
    private Long userId;
    private Long orderValue;
    private String orderStatus;
    private String email;

}
