package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Gerenciamento de endereços")
public class AddressController {

    private final AddressService addressService;

    @Operation(
            summary = "Listar meus endereços",
            description = "Retorna TODOS os endereços do usuário autenticado em formato de lista (array)."
    )
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> listAuthenticatedUserAddresses() {
        List<AddressResponseDTO> addresses = addressService.getUserAddresses();
        return ResponseEntity.ok(addresses);
    }

    @Operation(
            summary = "Listar todos os endereços (staff)",
            description = "Retorna todos os endereços cadastrados no sistema (uso interno)."
    )
    @GetMapping("/all")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Page<AddressResponseDTO>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AddressResponseDTO> addresses = addressService.findAllAddress(pageable);
        return ResponseEntity.ok(addresses);
    }

    @Deprecated
    @Operation(
            summary = "[DEPRECATED] Listar endereços por usuário",
            description = "Endpoint legado. Use GET /address para listar os endereços do usuário autenticado.",
            deprecated = true
    )
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO') or @usuarioService.getCurrentUsuario().id == #id")
    public ResponseEntity<Page<AddressResponseDTO>> findByUserId(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AddressResponseDTO> addresses = addressService.findAllByUserId(id, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Warning", "299 - \"Deprecated endpoint: use GET /address\"");
        return ResponseEntity.ok().headers(headers).body(addresses);
    }

    @Operation(
            summary = "Buscar endereço por ID",
            description = "Busca um endereço específico pelo ID do endereço."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO') or @addressService.isOwner(#id)")
    public ResponseEntity<AddressResponseDTO> findById(@PathVariable Long id) {
        AddressResponseDTO address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

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

    @Operation(
            summary = "Atualizar endereço",
            description = "Atualiza um endereço existente do usuário autenticado."
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO') or @addressService.isOwner(#id)")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Deletar endereço",
            description = "Realiza o soft delete de um endereço do usuário autenticado."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FUNCIONARIO') or @addressService.isOwner(#id)")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints legados de endereço principal foram removidos do contrato funcional do checkout.
}