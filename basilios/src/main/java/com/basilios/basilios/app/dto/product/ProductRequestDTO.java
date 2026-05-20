package com.basilios.basilios.app.dto.product;

import com.basilios.basilios.core.enums.AdicionalSubcategory;
import com.basilios.basilios.core.enums.ProductCategory;
import com.basilios.basilios.core.enums.ProductSubcategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO unificado para criação e atualização de produtos
 * Usado em POST /products e PATCH /products/{id}
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
    @org.hibernate.validator.constraints.URL(message = "URL da imagem inválida")
    private String imageUrl;

    @NotNull(message = "Categoria é obrigatória")
    private ProductCategory category;

    private ProductSubcategory subcategory; // Opcional

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido")
    private BigDecimal price;

    // Se informado, sincroniza os vínculos da tabela adicional_product por subcategoria.
    private List<AdicionalSubcategory> adicionalSubcategories;
}