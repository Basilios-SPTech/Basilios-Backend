package com.basilios.basilios.app.dto.menu;

import com.basilios.basilios.app.dto.product.ProductResponseDTO;
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
    private List<ProductResponseDTO> featured; // Produtos em destaque
    private List<ProductResponseDTO> popular; // Mais populares
    private List<ProductResponseDTO> recent; // Recém adicionados
    private List<ProductResponseDTO> affordable; // Mais baratos
    private List<ProductResponseDTO> premium; // Premium
    private List<ProductResponseDTO> similar; // Similares a um produto específico
}