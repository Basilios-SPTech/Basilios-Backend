package com.basilios.basilios.app.dto.product;

import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO básico de produto (usado internamente)
 * Para requests, use ProductRequest
 * Para responses, use ProductResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    private String name;

    private String description;

    private ProductCategory category;

    private ProductSubcategory subcategory;

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    private BigDecimal price;

    @Builder.Default
    private List<String> ingredientes = new ArrayList<>();

    private Boolean isPaused;
}