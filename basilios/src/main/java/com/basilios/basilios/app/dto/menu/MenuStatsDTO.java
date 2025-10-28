package com.basilios.basilios.app.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuStatsDTO {
    private Long totalProdutos;
    private Long produtosAtivos;
    private Long produtosPausados;
    private BigDecimal precoMedio;
    private BigDecimal menorPreco;
    private BigDecimal maiorPreco;
    private Long produtosEconomicos; // <= 15
    private Long produtosMedios; // 15-30
    private Long produtosPremium; // > 30
}