package com.basilios.basilios.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuFilterDTO {
    private String nome;

    @DecimalMin(value = "0.00", message = "Preço mínimo deve ser maior ou igual a zero")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.01", message = "Preço máximo deve ser maior que zero")
    private BigDecimal maxPrice;

    private List<String> ingredientes;

    private String categoria; // ECONOMICO, MEDIO, PREMIUM

    @Builder.Default
    private boolean activeOnly = true;

    @Builder.Default
    private String sortBy = "nome"; // nome, preco, createdAt

    @Builder.Default
    private String sortDirection = "asc"; // asc, desc

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}