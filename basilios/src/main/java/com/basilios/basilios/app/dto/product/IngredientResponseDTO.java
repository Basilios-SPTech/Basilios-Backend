package com.basilios.basilios.app.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponseDTO {
    private Long id;
    private String name;
    private Integer quantity;
    private String unit;
}
