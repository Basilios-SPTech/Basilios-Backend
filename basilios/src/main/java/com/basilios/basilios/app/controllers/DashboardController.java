package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.service.DashboardService;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas e relatórios agregados")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Visão geral", description = "Estatísticas principais do menu e vendas")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Mais vendidos", description = "Produtos mais vendidos")
    public ResponseEntity<List<Map<String, Object>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getBestSellers(limit));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Receita por dia", description = "Receita diária dos últimos N dias")
    public ResponseEntity<List<Map<String, Object>>> getRevenue(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getRevenueLastDays(days));
    }

    @GetMapping("/never-sold")
    @Operation(summary = "Nunca vendidos", description = "Produtos que nunca foram vendidos")
    public ResponseEntity<List<ProductResponseDTO>> getNeverSold() {
        return ResponseEntity.ok(dashboardService.getNeverSoldProducts());
    }

    @GetMapping("/products-without-ingredients")
    @Operation(summary = "Sem ingredientes", description = "Produtos sem ingredientes cadastrados")
    public ResponseEntity<List<ProductResponseDTO>> getProductsWithoutIngredients() {
        return ResponseEntity.ok(dashboardService.getProductsWithoutIngredients());
    }

    @GetMapping("/price-analysis")
    @Operation(summary = "Análise de preço", description = "Média/min/max de preços")
    public ResponseEntity<Map<String, Object>> getPriceAnalysis() {
        return ResponseEntity.ok(dashboardService.getPriceAnalysis());
    }
}

