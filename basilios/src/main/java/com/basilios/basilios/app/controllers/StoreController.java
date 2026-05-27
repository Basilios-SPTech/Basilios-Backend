package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.store.StoreHoursResponseDTO;
import com.basilios.basilios.app.dto.store.StoreHoursUpdateDTO;
import com.basilios.basilios.app.dto.store.StorePatchUpdateDTO;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.core.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/hours")
    public ResponseEntity<StoreHoursResponseDTO> getMainStoreHours() {
        return ResponseEntity.ok(storeService.getMainStoreHours());
    }

    @PutMapping("/hours")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<StoreHoursResponseDTO> updateMainStoreHours(@Valid @RequestBody StoreHoursUpdateDTO body) {
        return ResponseEntity.ok(storeService.updateMainStoreHours(body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Store> patchMainStore(@Valid @RequestBody StorePatchUpdateDTO body) {
        return ResponseEntity.ok(storeService.patchMainStore(body));
    }
}
