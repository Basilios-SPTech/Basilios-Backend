package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.app.dto.product.ProductResponseDTO.PromotionSummary;
import com.basilios.basilios.app.dto.product.ProductRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProductControllerTest {

    private FakeProductController fakeController;

    @BeforeEach
    void setup() {
        fakeController = new FakeProductController();
    }

    // ==================== CREATE ====================
    @Test
    void testCreateProduct() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Produto 1");
        request.setPrice(new BigDecimal("10.0"));

        ResponseEntity<ProductResponseDTO> response = fakeController.createProduct(request);

        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Produto 1", response.getBody().getName());
        assertFalse(response.getBody().getIsPaused());
    }

    // ==================== LIST ====================
    @Test
    void testListProducts() {
        ProductResponseDTO p1 = fakeController.createSampleProduct(1L, "P1", false);
        ProductResponseDTO p2 = fakeController.createSampleProduct(2L, "P2", true);

        ResponseEntity<List<ProductResponseDTO>> response = fakeController.listProducts(true);

        assertEquals(200, response.getStatusCodeValue());
        List<ProductResponseDTO> products = response.getBody();
        assertEquals(1, products.size()); // Apenas ativos
        assertEquals("P1", products.get(0).getName());
    }

    // ==================== GET ====================
    @Test
    void testGetProductById() {
        ProductResponseDTO p1 = fakeController.createSampleProduct(1L, "P1", false);

        ResponseEntity<ProductResponseDTO> resp = fakeController.getProduct(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("P1", resp.getBody().getName());

        ResponseEntity<ProductResponseDTO> respNotFound = fakeController.getProduct(999L);
        assertEquals(404, respNotFound.getStatusCodeValue());
    }

    // ==================== UPDATE ====================
    @Test
    void testUpdateProduct() {
        ProductResponseDTO p = fakeController.createSampleProduct(1L, "P1", false);

        ProductRequestDTO update = new ProductRequestDTO();
        update.setName("Produto Atualizado");
        update.setPrice(new BigDecimal("20.0"));

        ResponseEntity<ProductResponseDTO> resp = fakeController.updateProduct(1L, update);
        assertEquals("Produto Atualizado", resp.getBody().getName());
        assertEquals(new BigDecimal("20.0"), resp.getBody().getPrice());
    }

    // ==================== DELETE ====================
    @Test
    void testDeleteProduct() {
        fakeController.createSampleProduct(1L, "P1", false);
        ResponseEntity<Void> resp = fakeController.deleteProduct(1L);
        assertEquals(204, resp.getStatusCodeValue());

        // Verificar se realmente não existe
        ResponseEntity<ProductResponseDTO> resp404 = fakeController.getProduct(1L);
        assertEquals(404, resp404.getStatusCodeValue());
    }

    // ==================== PAUSE / ACTIVATE ====================
    @Test
    void testPauseAndActivateProduct() {
        fakeController.createSampleProduct(1L, "Produto X", false);

        ResponseEntity<ProductResponseDTO> pauseResp = fakeController.pauseProduct(1L);
        assertTrue(pauseResp.getBody().getIsPaused());

        ResponseEntity<ProductResponseDTO> activateResp = fakeController.activateProduct(1L);
        assertFalse(activateResp.getBody().getIsPaused());
    }

    // ==================== UPDATE PRICE ====================
    @Test
    void testUpdatePrice() {
        fakeController.createSampleProduct(1L, "Produto P", false);

        Map<String, Object> body = new HashMap<>();
        body.put("newPrice", 50.0);

        ResponseEntity<ProductResponseDTO> resp = fakeController.updatePrice(1L, body);
        assertEquals(new BigDecimal("50.0"), resp.getBody().getPrice());
    }

    // ==================== FAKE CONTROLLER ====================
    static class FakeProductController {

        private final Map<Long, ProductResponseDTO> fakeDb = new HashMap<>();
        private long idSequence = 1;

        public ProductResponseDTO createSampleProduct(Long id, String name, boolean paused) {
            ProductResponseDTO p = ProductResponseDTO.builder()
                    .id(id)
                    .name(name)
                    .price(new BigDecimal("10.0"))
                    .finalPrice(new BigDecimal("10.0"))
                    .isPaused(paused)
                    .build();
            fakeDb.put(id, p);
            return p;
        }

        public ResponseEntity<ProductResponseDTO> createProduct(ProductRequestDTO dto) {
            long id = idSequence++;
            ProductResponseDTO p = ProductResponseDTO.builder()
                    .id(id)
                    .name(dto.getName())
                    .price(dto.getPrice())
                    .finalPrice(dto.getPrice())
                    .isPaused(false)
                    .build();
            fakeDb.put(id, p);
            return ResponseEntity.status(201).body(p);
        }

        public ResponseEntity<List<ProductResponseDTO>> listProducts(boolean activeOnly) {
            List<ProductResponseDTO> list = new ArrayList<>();
            for (ProductResponseDTO p : fakeDb.values()) {
                if (!activeOnly || !p.getIsPaused()) list.add(p);
            }
            return ResponseEntity.ok(list);
        }

        public ResponseEntity<ProductResponseDTO> getProduct(Long id) {
            ProductResponseDTO p = fakeDb.get(id);
            if (p == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(p);
        }

        public ResponseEntity<ProductResponseDTO> updateProduct(Long id, ProductRequestDTO dto) {
            ProductResponseDTO p = fakeDb.get(id);
            if (p == null) return ResponseEntity.notFound().build();
            p.setName(dto.getName());
            p.setPrice(dto.getPrice());
            p.setFinalPrice(dto.getPrice());
            return ResponseEntity.ok(p);
        }

        public ResponseEntity<Void> deleteProduct(Long id) {
            fakeDb.remove(id);
            return ResponseEntity.noContent().build();
        }

        public ResponseEntity<ProductResponseDTO> pauseProduct(Long id) {
            ProductResponseDTO p = fakeDb.get(id);
            if (p == null) return ResponseEntity.notFound().build();
            p.setIsPaused(true);
            return ResponseEntity.ok(p);
        }

        public ResponseEntity<ProductResponseDTO> activateProduct(Long id) {
            ProductResponseDTO p = fakeDb.get(id);
            if (p == null) return ResponseEntity.notFound().build();
            p.setIsPaused(false);
            return ResponseEntity.ok(p);
        }

        public ResponseEntity<ProductResponseDTO> updatePrice(Long id, Map<String, Object> body) {
            ProductResponseDTO p = fakeDb.get(id);
            if (p == null) return ResponseEntity.notFound().build();
            Object np = body.get("newPrice");
            BigDecimal newPrice = null;
            if (np instanceof Number) {
                newPrice = BigDecimal.valueOf(((Number) np).doubleValue());
            } else if (np instanceof String) {
                newPrice = new BigDecimal((String) np);
            }
            p.setPrice(newPrice);
            p.setFinalPrice(newPrice);
            return ResponseEntity.ok(p);
        }
    }
}
