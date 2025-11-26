package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.service.AddressService;
import com.basilios.basilios.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Gerenciamento de endereços")
public class AddressController {

    private final AddressService addressService;
    private final OrderService orderService;

    // ========== GET - LISTAGEM ==========


    @Operation(
            summary = "Listar todos os endereços",
            description = "Retorna TODOS os endereços cadastrados no sistema."
    )
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> findAll() {
        List<AddressResponseDTO> addresses = addressService.findAllAddress();
        return ResponseEntity.ok(addresses);
    }

    @Operation(
            summary = "Buscar endereço por ID",
            description = "Busca um endereço específico pelo ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> findById(@PathVariable Long id) {
        AddressResponseDTO address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

    @Operation(
            summary = "Listar endereços do usuário",
            description = "Retorna todos os endereços de um usuário específico."
    )
    @GetMapping("/user/{id}")
    public ResponseEntity<List<AddressResponseDTO>> findByUserId(@PathVariable Long id) {
        List<AddressResponseDTO> addresses = addressService.findAllByUserId(id);
        return ResponseEntity.ok(addresses);
    }

    @Operation(
            summary = "Buscar endereço principal do usuário",
            description = "Retorna o endereço principal do usuário autenticado."
    )
    @GetMapping("/principal")
    public ResponseEntity<AddressResponseDTO> getPrincipalAddress() {
        AddressResponseDTO address = addressService.getPrincipalAddress();
        return ResponseEntity.ok(address);
    }

    // ========== POST - CRIAR ==========

    @Operation(
            summary = "Criar novo endereço",
            description = "Cria um novo endereço para o usuário autenticado."
    )
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== PATCH - ATUALIZAR ==========

    @Operation(
            summary = "Atualizar endereço",
            description = "Atualiza um endereço existente."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Definir endereço como principal",
            description = "Define um endereço específico como principal para o usuário autenticado."
    )
    @PatchMapping("/{id}/principal")
    public ResponseEntity<AddressResponseDTO> setAsPrincipal(@PathVariable Long id) {
        AddressResponseDTO response = addressService.setAsPrincipal(id);
        return ResponseEntity.ok(response);
    }

    // ========== DELETE ==========

    @Operation(
            summary = "Deletar endereço",
            description = "Realiza o soft delete de um endereço."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}