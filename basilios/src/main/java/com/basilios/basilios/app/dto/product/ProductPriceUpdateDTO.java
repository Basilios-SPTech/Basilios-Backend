package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPriceUpdateDTO {
    @NotNull(message = "O novo preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal newPrice;
}
