package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientRequestDTO {
    @NotBlank(message = "O nome do ingrediente é obrigatório")
    private String name;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private Integer qty = 1;

    private String unit = "unidade";
}
