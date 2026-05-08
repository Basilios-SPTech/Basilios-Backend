package com.basilios.basilios.app.controllers;

import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.core.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Store> createStore(@Valid @RequestBody Store store) {
        Store created = storeService.create(store);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(storeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/delivery-fee")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Store> updateDeliveryFee(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal deliveryFee = body.get("deliveryFee");
        if (deliveryFee == null || deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(storeService.updateDeliveryFee(deliveryFee));
    }
}
