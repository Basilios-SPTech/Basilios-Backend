package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "CRUD e operações administrativas sobre produtos")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um novo produto (ROLE_FUNCIONARIO)")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista produtos; activeOnly=true por padrão")
    public ResponseEntity<List<ProductResponseDTO>> listProducts(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<ProductResponseDTO> list = productService.getAllProducts(activeOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar produto", description = "Retorna detalhes do produto por id")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        ProductResponseDTO dto = productService.getProductById(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza dados do produto")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto", description = "Deleta produto se permitido")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Status e Preço ==========

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pausar produto", description = "Pausa produto do cardápio")
    public ResponseEntity<ProductResponseDTO> pauseProduct(@PathVariable Long id) {
        ProductResponseDTO dto = productService.pauseProduct(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Ativar produto", description = "Ativa produto no cardápio")
    public ResponseEntity<ProductResponseDTO> activateProduct(@PathVariable Long id) {
        ProductResponseDTO dto = productService.activateProduct(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Atualizar preço", description = "Atualiza apenas o preço do produto")
    public ResponseEntity<ProductResponseDTO> updatePrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Object np = body.get("newPrice");
        BigDecimal newPrice = null;
        if (np instanceof Number) {
            newPrice = BigDecimal.valueOf(((Number) np).doubleValue());
        } else if (np instanceof String) {
            newPrice = new BigDecimal((String) np);
        }
        ProductResponseDTO dto = productService.updatePrice(id, newPrice);
        return ResponseEntity.ok(dto);
    }

    // ========== Ingredientes ==========

    @GetMapping("/{id}/ingredients")
    @Operation(summary = "Listar ingredientes", description = "Lista ingredientes de um produto")
    public ResponseEntity<List<Map<String, Object>>> getIngredients(@PathVariable Long id) {
        List<Map<String, Object>> list = productService.getProductIngredients(id);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/ingredients")
    @Operation(summary = "Adicionar ingrediente", description = "Adiciona ingrediente ao produto")
    public ResponseEntity<ProductResponseDTO> addIngredient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer qty = body.get("qty") instanceof Number ? ((Number) body.get("qty")).intValue() : null;
        String unit = body.get("unit") != null ? (String) body.get("unit") : null;
        ProductResponseDTO dto = productService.addIngredient(id, name, qty, unit);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}/ingredients/{ingredientId}")
    @Operation(summary = "Remover ingrediente", description = "Remove ingrediente do produto")
    public ResponseEntity<ProductResponseDTO> removeIngredient(
            @PathVariable Long id,
            @PathVariable Long ingredientId) {
        ProductResponseDTO dto = productService.removeIngredient(id, ingredientId);
        return ResponseEntity.ok(dto);
    }

    // ========== Estatísticas / Relatórios ==========

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de produtos", description = "Estatísticas gerais do cardápio")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = productService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/price-analysis")
    @Operation(summary = "Análise de preço", description = "Média/min/max de preços")
    public ResponseEntity<Map<String, java.math.BigDecimal>> getPriceAnalysis() {
        Map<String, java.math.BigDecimal> stats = productService.getPriceAnalysis();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/sales-stats")
    @Operation(summary = "Estatísticas de vendas por produto")
    public ResponseEntity<Map<String, Object>> getSalesStats(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getSalesStatistics(id));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Mais vendidos", description = "Lista produtos mais vendidos")
    public ResponseEntity<List<Map<String, Object>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getBestSellers(limit));
    }

    @GetMapping("/never-sold")
    @Operation(summary = "Nunca vendidos", description = "Produtos nunca vendidos")
    public ResponseEntity<List<ProductResponseDTO>> getNeverSold() {
        return ResponseEntity.ok(productService.getNeverSoldProducts());
    }

    @GetMapping("/low-usage")
    @Operation(summary = "Baixo uso", description = "Produtos com vendas abaixo de minSales")
    public ResponseEntity<List<Map<String, Object>>> getLowUsage(
            @RequestParam(defaultValue = "5") int minSales) {
        return ResponseEntity.ok(productService.getLowUsageProducts(minSales));
    }

    @GetMapping("/on-promotion")
    @Operation(summary = "Em promoção", description = "Produtos atualmente em promoção")
    public ResponseEntity<List<ProductResponseDTO>> getOnPromotion() {
        return ResponseEntity.ok(productService.getProductsOnPromotion());
    }

    @GetMapping("/{id}/promotions")
    @Operation(summary = "Promoções de produto", description = "Promoções vigentes para um produto")
    public ResponseEntity<List<Map<String, Object>>> getProductPromotions(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductPromotions(id));
    }

    @GetMapping("/{id}/combos")
    @Operation(summary = "Combos do produto", description = "Combos que contêm o produto")
    public ResponseEntity<List<Map<String, Object>>> getProductCombos(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductCombos(id));
    }

    @GetMapping("/most-used-in-combos")
    @Operation(summary = "Mais usados em combos", description = "Produtos mais usados em combos")
    public ResponseEntity<List<Map<String, Object>>> getMostUsedInCombos(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getMostUsedInCombos(limit));
    }

    @GetMapping("/by-price-category")
    @Operation(summary = "Por categoria de preço", description = "ECONOMIC, MEDIUM, PREMIUM")
    public ResponseEntity<List<ProductResponseDTO>> getByPriceCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(productService.getByCategory(category, activeOnly));
    }

    @GetMapping("/category-statistics")
    @Operation(summary = "Estatísticas por categoria", description = "Contagem por categoria de preço")
    public ResponseEntity<Map<String, Long>> getCategoryStatistics() {
        return ResponseEntity.ok(productService.getCategoryStatistics());
    }

    @PostMapping("/validate-availability")
    @Operation(summary = "Validar disponibilidade", description = "Valida disponibilidade de vários produtos")
    public ResponseEntity<Map<String, Object>> validateAvailability(
            @RequestBody List<Long> productIds) {
        return ResponseEntity.ok(productService.validateAvailability(productIds));
    }

    @GetMapping("/can-delete/{id}")
    @Operation(summary = "Pode deletar?", description = "Verifica se produto pode ser deletado")
    public ResponseEntity<Map<String, Object>> canDelete(@PathVariable Long id) {
        return ResponseEntity.ok(productService.canDeleteProduct(id));
    }

    @GetMapping("/{id}/pause-impact")
    @Operation(summary = "Impacto ao pausar", description = "Impacto estimado de pausar um produto")
    public ResponseEntity<Map<String, Object>> pauseImpact(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getPauseImpact(id));
    }
}
