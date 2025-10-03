package com.basilios.basilios.app.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSuggestionDTO {
    private List<ProdutoResponseDTO> featured; // Produtos em destaque
    private List<ProdutoResponseDTO> popular; // Mais populares
    private List<ProdutoResponseDTO> recent; // Recém adicionados
    private List<ProdutoResponseDTO> affordable; // Mais baratos
    private List<ProdutoResponseDTO> premium; // Premium
    private List<ProdutoResponseDTO> similar; // Similares a um produto específico
}