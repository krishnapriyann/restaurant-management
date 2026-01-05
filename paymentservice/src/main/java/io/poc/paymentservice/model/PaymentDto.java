package io.poc.paymentservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentDto {
//    private Long paymentId;
    private Long orderId;
    private Long amount;
    private String paymentType;
    private String status;
}
