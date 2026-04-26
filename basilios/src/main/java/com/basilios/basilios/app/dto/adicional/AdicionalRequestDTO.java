package com.basilios.basilios.app.dto.adicional;

import com.basilios.basilios.core.enums.AdicionalSubcategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalRequestDTO {

    @NotBlank(message = "Nome do adicional é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    private AdicionalSubcategory subcategory;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido")
    private BigDecimal price;
}
