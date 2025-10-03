package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Produto;
import com.basilios.basilios.core.service.MenuService;
import com.basilios.basilios.app.dto.menu.ProdutoDTO;
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
    @GetMapping
    public ResponseEntity<List<Produto>> getActiveMenu() {
        List<Produto> produtos = menuService.getActiveMenu();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Retorna todos os produtos (ativos e pausados)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Produto>> getAllMenu() {
        List<Produto> produtos = menuService.getAllMenu();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Retorna produtos com paginação
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Produto>> getMenuPaginated(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        Page<Produto> produtos = menuService.getMenuPaginated(activeOnly, pageable);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produto> getProdutoById(@PathVariable Long id) {
        Produto produto = menuService.getProdutoById(id);
        return ResponseEntity.ok(produto);
    }

    /**
     * Busca produtos por nome (contém)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Produto>> searchProdutosByNome(
            @RequestParam String nome,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Produto> produtos = menuService.searchByNome(nome, activeOnly);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produtos por faixa de preço
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<Produto>> getProdutosByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Produto> produtos = menuService.getProdutosByPriceRange(minPrice, maxPrice, activeOnly);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produtos que contêm ingrediente específico
     */
    @GetMapping("/ingredient")
    public ResponseEntity<List<Produto>> getProdutosByIngredient(
            @RequestParam String ingrediente,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Produto> produtos = menuService.getProdutosByIngredient(ingrediente, activeOnly);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produtos ordenados por preço
     */
    @GetMapping("/ordered-by-price")
    public ResponseEntity<List<Produto>> getProdutosOrderedByPrice(
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<Produto> produtos = menuService.getProdutosOrderedByPrice(direction, activeOnly);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca com múltiplos filtros
     */
    @PostMapping("/filter")
    public ResponseEntity<List<Produto>> getFilteredMenu(@Valid @RequestBody MenuFilterDTO filter) {
        List<Produto> produtos = menuService.getFilteredMenu(filter);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Criar novo produto
     */
    @PostMapping
    public ResponseEntity<Produto> createProduto(@Valid @RequestBody ProdutoDTO produtoDTO) {
        Produto produto = menuService.createProduto(produtoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    /**
     * Atualizar produto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Produto> updateProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoDTO produtoDTO) {
        Produto produto = menuService.updateProduto(id, produtoDTO);
        return ResponseEntity.ok(produto);
    }

    /**
     * Pausar produto (soft pause)
     */
    @PatchMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseProduto(@PathVariable Long id) {
        menuService.pauseProduto(id);
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
        menuService.activateProduto(id);
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
        boolean newStatus = menuService.toggleProdutoStatus(id);
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
        menuService.deleteProduto(id);
        return ResponseEntity.ok(Map.of("message", "Produto removido com sucesso"));
    }

    /**
     * Atualizar preço do produto
     */
    @PatchMapping("/{id}/price")
    public ResponseEntity<Produto> updateProdutoPrice(
            @PathVariable Long id,
            @RequestParam BigDecimal novoPreco) {
        Produto produto = menuService.updateProdutoPrice(id, novoPreco);
        return ResponseEntity.ok(produto);
    }

    /**
     * Buscar produtos mais vendidos (se houver implementação futura)
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Produto>> getPopularProdutos(
            @RequestParam(defaultValue = "10") int limit) {
        List<Produto> produtos = menuService.getPopularProdutos(limit);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Contar produtos ativos
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getMenuStats() {
        Map<String, Long> stats = Map.of(
                "totalProdutos", menuService.countAllProdutos(),
                "produtosAtivos", menuService.countActiveProdutos(),
                "produtosPausados", menuService.countPausedProdutos()
        );
        return ResponseEntity.ok(stats);
    }

    /**
     * Verificar disponibilidade do produto
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<Map<String, Object>> checkProdutoAvailability(@PathVariable Long id) {
        boolean available = menuService.isProdutoAvailable(id);
        Produto produto = menuService.getProdutoById(id);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "nome", produto.getNomeProduto(),
                "available", available,
                "paused", produto.getIsPaused()
        ));
    }
}