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
    private Boolean activeOnly = true;
    private Integer limit = 10;
    private String sortBy = "relevance"; // relevance, price, name, date
    private String sortDirection = "desc";
}