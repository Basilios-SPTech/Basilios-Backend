package com.basilios.basilios.app.dto.adicional;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import com.basilios.basilios.core.enums.AdicionalSubcategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalUpdateDTO {

    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    private AdicionalSubcategory subcategory;

    @DecimalMin(value = "0.00", message = "Preço deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido")
    private BigDecimal price;

    private Boolean available;
}
