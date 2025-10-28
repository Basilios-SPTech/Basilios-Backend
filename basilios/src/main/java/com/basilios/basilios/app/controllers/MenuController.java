package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.service.MenuService;
import com.basilios.basilios.app.dto.product.ProductDTO;
import com.basilios.basilios.app.dto.menu.MenuFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * Retorna apenas produtos ativos (não pausados)
     */
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveMenu() {
        List<Product> products = menuService.getActiveMenu();
        return ResponseEntity.ok(products);
    }

    /**
     * Retorna todos os produtos (ativos e pausados)
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllMenu() {
        List<Product> products = menuService.getAllMenu();
        return ResponseEntity.ok(products);
    }

    /**
     * Retorna produtos com paginação
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Product>> getMenuPaginated(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        Page<Product> produtos = menuService.getMenuPaginated(activeOnly, pageable);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProdutoById(@PathVariable Long id) {
        Product product = menuService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Busca produtos por nome (contém)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProdutosByNome(
            @RequestParam String nome,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.searchByName(nome, activeOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca produtos por faixa de preço
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProdutosByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsByPriceRange(minPrice, maxPrice, activeOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca produtos que contêm ingrediente específico
     */
    @GetMapping("/ingredient")
    public ResponseEntity<List<Product>> getProdutosByIngredient(
            @RequestParam String ingrediente,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsByIngredient(ingrediente, activeOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca produtos ordenados por preço
     */
    @GetMapping("/ordered-by-price")
    public ResponseEntity<List<Product>> getProdutosOrderedByPrice(
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Product> products = menuService.getProductsOrderedByPrice(direction, activeOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca com múltiplos filtros
     */
    @PostMapping("/filter")
    public ResponseEntity<List<Product>> getFilteredMenu(@Valid @RequestBody MenuFilterDTO filter) {
        List<Product> products = menuService.getFilteredMenu(filter);
        return ResponseEntity.ok(products);
    }

    /**
     * Criar novo produto
     */
    @PostMapping
    public ResponseEntity<Product> createProduto(@Valid @RequestBody ProductDTO productDTO) {
        Product product = menuService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Atualizar produto existente
     */
//    @PutMapping("/{id}")
//    public ResponseEntity<Product> updateProduto(
//            @PathVariable Long id,
//            @Valid @RequestBody ProductDTO productDTO) {
//        Product product = menuService.updateProduct(id, productDTO);
//        return ResponseEntity.ok(product);
//    }

    /**
     * Pausar produto (soft pause)
     */
    @PatchMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseProduto(@PathVariable Long id) {
        menuService.pauseProduct(id);
        return ResponseEntity.ok(Map.of(
                "message", "Produto pausado com sucesso",
                "id", id,
                "paused", true
        ));
    }

    /**
     * Ativar produto
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateProduto(@PathVariable Long id) {
        menuService.activateProduct(id);
        return ResponseEntity.ok(Map.of(
                "message", "Produto ativado com sucesso",
                "id", id,
                "paused", false
        ));
    }

    /**
     * Alternar status do produto (pausar/ativar)
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleProdutoStatus(@PathVariable Long id) {
        boolean newStatus = menuService.toggleProductStatus(id);
        return ResponseEntity.ok(Map.of(
                "message", "Status do produto alterado com sucesso",
                "id", id,
                "paused", newStatus
        ));
    }

    /**
     * Deletar produto (soft delete se implementado)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduto(@PathVariable Long id) {
        menuService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Produto removido com sucesso"));
    }

    /**
     * Atualizar preço do produto
     */
    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updateProdutoPrice(
            @PathVariable Long id,
            @RequestParam BigDecimal novoPreco) {
        Product product = menuService.updateProductPrice(id, novoPreco);
        return ResponseEntity.ok(product);
    }

    /**
     * Buscar produtos mais vendidos (se houver implementação futura)
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Product>> getPopularProdutos(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = menuService.getPopularProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Contar produtos ativos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getMenuStats() {
        Map<String, Long> stats = Map.of(
                "totalProdutos", menuService.countAllProducts(),
                "produtosAtivos", menuService.countActiveProducts(),
                "produtosPausados", menuService.countPausedProducts()
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * Verificar disponibilidade do produto
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<Map<String, Object>> checkProdutoAvailability(@PathVariable Long id) {
        boolean available = menuService.isProductAvailable(id);
        Product product = menuService.getProductById(id);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "nome", product.getName(),
                "available", available,
                "paused", product.getIsPaused()
        ));
    }
}