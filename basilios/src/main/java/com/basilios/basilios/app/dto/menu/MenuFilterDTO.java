package com.basilios.basilios.app.dto.menu;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "name|price|category|createdAt", message = "sortBy inválido. Use: name, price, category ou createdAt")
    private String sortBy = "name";

    @Builder.Default
    @Pattern(regexp = "asc|desc", message = "sortDirection inválido. Use: asc ou desc")
    private String sortDirection = "asc";

    @Builder.Default
    private int page = 0;

    @Builder.Default
    @Max(value = 100, message = "Tamanho máximo de página é 100")
    private int size = 20;

}
