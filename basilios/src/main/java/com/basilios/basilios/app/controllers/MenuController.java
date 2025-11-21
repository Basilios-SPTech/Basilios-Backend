package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.service.MenuService;
import com.basilios.basilios.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MenuController - Responsável APENAS por operações de LEITURA
 *
 * Endpoint: GET /menu (público, sem autenticação)
 *
 * Para operações de ESCRITA (criar, atualizar, deletar), use:
 * POST/PATCH/DELETE /api/products (autenticado, role FUNCIONARIO)
 */
@RestController
@RequestMapping("/menu")
@Tag(name = "Menu", description = "Consulta do cardápio (Público)")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private ProductService productService;

    // ========== MENU BÁSICO ==========

    @GetMapping("/active")
    @Operation(summary = "Cardápio completo", description = "Retorna todos os produtos ativos")
    public ResponseEntity<List<ProductResponseDTO>> getActiveMenu() {
        List<Product> products = menuService.getActiveMenu();
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    @Operation(summary = "Todos os produtos", description = "Retorna produtos ativos e inativos (admin)")
    public ResponseEntity<List<ProductResponseDTO>> getAllMenu() {
        List<Product> products = menuService.getAllMenu();
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Produtos com paginação", description = "Retorna produtos paginados")
    public ResponseEntity<Page<ProductResponseDTO>> getMenuPaginated(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        Page<Product> produtos = menuService.getMenuPaginated(activeOnly, pageable);
        List<ProductResponseDTO> dtos = produtos.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        Page<ProductResponseDTO> dtoPage = new PageImpl<>(dtos, pageable, produtos.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    // ========== BUSCAS ESPECÍFICAS ==========

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna detalhes do produto")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "ID do produto") @PathVariable Long id) {
        Product product = menuService.getProductById(id);
        return ResponseEntity.ok(productService.convertEntityToDTO(product));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar por nome", description = "Busca produtos pelo nome")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(
            @RequestParam String nome,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.searchByName(nome, activeOnly);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-price-range")
    @Operation(summary = "Por faixa de preço", description = "Busca produtos em uma faixa de preço")
    public ResponseEntity<List<ProductResponseDTO>> getByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsByPriceRange(minPrice, maxPrice, activeOnly);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-ingredient")
    @Operation(summary = "Por ingrediente", description = "Busca produtos que contêm um ingrediente")
    public ResponseEntity<List<ProductResponseDTO>> getByIngredient(
            @RequestParam String ingrediente,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsByIngredient(ingrediente, activeOnly);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ordered-by-price")
    @Operation(summary = "Ordenado por preço", description = "Retorna produtos ordenados por preço")
    public ResponseEntity<List<ProductResponseDTO>> getOrderedByPrice(
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsOrderedByPrice(direction, activeOnly);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-price-category")
    @Operation(summary = "Por categoria de preço", description = "ECONOMIC, MEDIUM ou PREMIUM")
    public ResponseEntity<List<ProductResponseDTO>> getByPriceCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsByPriceCategory(category, activeOnly);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ========== FILTRO AVANÇADO ==========

    @PostMapping("/filter")
    @Operation(summary = "Busca avançada", description = "Busca com múltiplos filtros (nome, preço, etc)")
    public ResponseEntity<List<ProductResponseDTO>> getFilteredMenu(@Valid @RequestBody MenuFilterDTO filter) {
        List<Product> products = menuService.getFilteredMenu(filter);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ========== PRODUTOS DESTACADOS ==========

    @GetMapping("/popular")
    @Operation(summary = "Produtos recentes", description = "Retorna produtos recém-adicionados")
    public ResponseEntity<List<ProductResponseDTO>> getPopularProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = menuService.getPopularProducts(limit);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/recently-added")
    @Operation(summary = "Adicionados recentemente", description = "Produtos adicionados recentemente")
    public ResponseEntity<List<ProductResponseDTO>> getRecentlyAdded(
            @RequestParam(defaultValue = "5") int limit) {
        List<Product> products = menuService.getRecentlyAddedProducts(limit);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Produtos similares", description = "Retorna produtos similares ao informado")
    public ResponseEntity<List<ProductResponseDTO>> getSimilarProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "burger") String keyword,
            @RequestParam(defaultValue = "5") int limit) {
        List<Product> products = menuService.getSimilarProducts(id, keyword, limit);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/suggestions")
    @Operation(summary = "Sugestões", description = "Retorna sugestões baseado no produto")
    public ResponseEntity<List<ProductResponseDTO>> getSuggestions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "3") int limit) {
        List<Product> products = menuService.getProductSuggestions(id, limit);
        List<ProductResponseDTO> dtos = products.stream()
                .map(productService::convertEntityToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ========== VALIDAÇÕES ==========

    @GetMapping("/{id}/available")
    @Operation(summary = "Verificar disponibilidade", description = "Verifica se produto está disponível")
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable Long id) {
        boolean available = menuService.isProductAvailable(id);
        Product product = menuService.getProductById(id);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "name", product.getName(),
                "available", available,
                "paused", product.getIsPaused()
        ));
    }

    @PostMapping("/validate-availability")
    @Operation(summary = "Validar múltiplos", description = "Valida disponibilidade de vários produtos")
    public ResponseEntity<Map<String, Object>> validateAvailability(
            @RequestBody List<Long> productIds) {
        try {
            menuService.validateProductsAvailability(productIds);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Todos os produtos estão disponíveis"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ========== ESTATÍSTICAS ==========

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas", description = "Retorna estatísticas do cardápio")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Object[] stats = menuService.getMenuStatistics();

        return ResponseEntity.ok(Map.of(
                "totalProdutos", stats[0],
                "produtosAtivos", stats[1],
                "produtosPausados", stats[2],
                "precoMedio", stats[3] != null ? stats[3] : BigDecimal.ZERO,
                "menorPreco", stats[4] != null ? stats[4] : BigDecimal.ZERO,
                "maiorPreco", stats[5] != null ? stats[5] : BigDecimal.ZERO
        ));
    }

    @GetMapping("/count")
    @Operation(summary = "Contadores", description = "Retorna contagens gerais")
    public ResponseEntity<Map<String, Long>> getMenuStats() {
        return ResponseEntity.ok(Map.of(
                "totalProdutos", menuService.countAllProducts(),
                "produtosAtivos", menuService.countActiveProducts(),
                "produtosPausados", menuService.countPausedProducts()
        ));
    }
}