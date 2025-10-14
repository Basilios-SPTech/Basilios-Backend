package com.basilios.basilios.app.dto.menu;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    private String name;

    @PositiveOrZero(message = "Minimum price must be zero or greater")
    private BigDecimal minPrice;

    @Positive(message = "Maximum price must be greater than zero")
    private BigDecimal maxPrice;


    private List<String> ingredients;

    private String category; // ECONOMIC, MEDIUM, PREMIUM

    @Builder.Default
    private boolean activeOnly = true;

    @Builder.Default
    private String sortBy = "name"; // name, price, createdAt

    @Builder.Default
    private String sortDirection = "asc"; // asc, desc

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

}
