package com.basilios.basilios.app.dto.product;

import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> tags = new ArrayList<>();

    private BigDecimal price;

    @Builder.Default
    private List<String> ingredientes = new ArrayList<>();

    private Boolean isPaused;
}