package io.poc.inventoryservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FoodDto {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private int stock;

}

