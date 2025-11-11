package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // ====================== GETs ======================

    /**
     * Lista todos os endereços do usuário autenticado
     */
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses() {
        return ResponseEntity.ok(addressService.getUserAddresses());
    }

    /**
     * Lista todos os endereços de um usuário específico
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<Address>> getAddressesByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(addressService.findActiveAddressesByUserId(usuarioId));
    }

    /**
     * Busca um endereço específico do usuário autenticado
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<AddressResponseDTO> getUserAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAuthenticatedUserAddressById(id));
    }

    /**
     * Retorna o endereço principal do usuário autenticado
     */
    @GetMapping("/principal")
    public ResponseEntity<AddressResponseDTO> getPrincipalAddress() {
        return ResponseEntity.ok(addressService.getPrincipalAddress());
    }

    // ====================== POST ======================

    /**
     * Cria um novo endereço para o usuário autenticado
     */
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.createAddress(request);
        return ResponseEntity.status(201).body(response);
    }

    // ====================== PUT ======================

    /**
     * Atualiza um endereço existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    /**
     * Define um endereço como principal
     */
    @PutMapping("/{id}/principal")
    public ResponseEntity<AddressResponseDTO> setAsPrincipal(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setAsPrincipal(id));
    }

    // ====================== DELETE ======================

    /**
     * Deleta (soft delete) um endereço do usuário autenticado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    // ====================== PATCH ======================

    /**
     * Restaura um endereço deletado
     */
    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<AddressResponseDTO> restoreAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.restoreAddress(id));
    }

    // ====================== ESTATÍSTICAS ======================

    @GetMapping("/ativos/count")
    public ResponseEntity<Long> countUserActiveAddresses() {
        return ResponseEntity.ok(addressService.countUserActiveAddresses());
    }

    @GetMapping("/tem-endereco")
    public ResponseEntity<Boolean> hasAddresses() {
        return ResponseEntity.ok(addressService.hasAddresses());
    }

    @GetMapping("/tem-principal")
    public ResponseEntity<Boolean> hasPrincipalAddress() {
        return ResponseEntity.ok(addressService.hasPrincipalAddress());
    }
}
