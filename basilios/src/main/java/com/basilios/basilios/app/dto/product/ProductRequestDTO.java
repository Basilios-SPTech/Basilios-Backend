package com.basilios.basilios.app.dto.product;

import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO unificado para criação e atualização de produtos
 * Usado em POST /products e PUT /products/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
    private String description;

    // URL da imagem do produto (opcional no back, mas o front vai preencher)
    private String imageUrl;

    @NotNull(message = "Categoria é obrigatória")
    private ProductCategory category;

    private ProductSubcategory subcategory; // Opcional

    @Builder.Default
    private List<String> tags = new ArrayList<>(); // ["ARTESANAL", "PICANTE", "VEGANO"]

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido")
    private BigDecimal price;

    @Builder.Default
    private List<String> ingredientes = new ArrayList<>(); // Lista simples de ingredientes

    @Builder.Default
    private List<IngredientDetail> ingredientsDetailed = new ArrayList<>(); // Ingredientes com detalhes

    /**
     * Inner class para ingredientes detalhados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDetail {

        @NotBlank(message = "Nome do ingrediente é obrigatório")
        private String name;

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        private Double quantity;

        @NotBlank(message = "Unidade de medida é obrigatória")
        private String measurementUnit; // "g", "ml", "unidade", "fatias"
    }
}