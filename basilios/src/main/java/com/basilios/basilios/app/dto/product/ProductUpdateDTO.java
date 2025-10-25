package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
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
public class ProductUpdateDTO {
    @Size(max = 255, message = "Nome do produto deve ter no máximo 255 caracteres")
    private String nomeProduto;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private List<@Size(max = 100, message = "Ingrediente deve ter no máximo 100 caracteres") String> ingredientes;

    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    private Boolean isPaused;
}