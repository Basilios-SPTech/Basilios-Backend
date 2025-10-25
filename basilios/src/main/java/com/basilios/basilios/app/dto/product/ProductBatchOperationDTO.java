package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ProductBatchOperationDTO {
    @NotEmpty(message = "Lista de IDs não pode estar vazia")
    private List<Long> produtoIds;

    @NotNull(message = "Operação é obrigatória")
    private OperationType operation;

    // Usado para operações de atualização de preço
    private BigDecimal novoPreco;

    // Usado para operações de atualização de categoria
    private String novaCategoria;

    public enum OperationType {
        PAUSE,
        ACTIVATE,
        DELETE,
        UPDATE_PRICE,
        UPDATE_CATEGORY
    }
}
