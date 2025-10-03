package com.basilios.basilios.app.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponseDTO {
    private Long idProduto;
    private String nomeProduto;
    private String descricao;
    private List<String> ingredientes;
    private BigDecimal preco;
    private Boolean isPaused;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String categoria; // Baseada no preço
    private Boolean isAvailable;
    private List<ProdutoResponseDTO> similar; // Produtos similares
}