package com.basilios.basilios.app.dto.product;

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
public class ProductSearchDTO {
    private String query;
    private List<String> searchFields; // nome, descricao, ingredientes
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> categories;
    @Builder.Default
    private Boolean activeOnly = true;
    @Builder.Default
    private Integer limit = 10;
    @Builder.Default
    private String sortBy = "relevance"; // relevance, price, name, date
    @Builder.Default
    private String sortDirection = "desc";
}