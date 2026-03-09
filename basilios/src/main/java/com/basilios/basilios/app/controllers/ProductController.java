package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.app.dto.product.ProductStatusDTO;
import com.basilios.basilios.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "CRUD e operações administrativas sobre produtos")
public class ProductController {

    @Autowired
    private ProductService productService;
    @PreAuthorize("hasRole('FUNCIONARIO')")
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
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping("/{id}")
    @Operation(summary = "acha por id o produto", description = "Retorna o produto por id")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        ProductResponseDTO dto = productService.getProductById(id);
        return ResponseEntity.ok(dto);
    }
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza dados do produto")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto", description = "Deleta produto se permitido")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Status e Preço ===========
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do produto", description = "Atualiza apenas o campo isPaused do produto (true/false)")
    public ResponseEntity<ProductResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusDTO statusDTO) {
        ProductResponseDTO dto = productService.updateStatus(id, statusDTO.getIsPaused());
        return ResponseEntity.ok(dto);
    }
    @PreAuthorize("hasRole('FUNCIONARIO')")
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
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @GetMapping("/{id}/ingredients")
    @Operation(summary = "Listar ingredientes", description = "Lista ingredientes de um produto")
    public ResponseEntity<List<Map<String, Object>>> getIngredients(@PathVariable Long id) {
        List<Map<String, Object>> list = productService.getProductIngredients(id);
        return ResponseEntity.ok(list);
    }
    @PreAuthorize("hasRole('FUNCIONARIO')")
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
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @DeleteMapping("/{id}/ingredients/{ingredientId}")
    @Operation(summary = "Remover ingrediente", description = "Remove ingrediente do produto")
    public ResponseEntity<ProductResponseDTO> removeIngredient(
            @PathVariable Long id,
            @PathVariable Long ingredientId) {
        ProductResponseDTO dto = productService.removeIngredient(id, ingredientId);
        return ResponseEntity.ok(dto);
    }
}
