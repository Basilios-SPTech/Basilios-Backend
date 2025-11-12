package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.service.AddressService;
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
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Gerencia os endereços do usuário autenticado")
public class AddressController {

    private final AddressService addressService;

    // ========== LISTAGEM ==========

    @Operation(
            summary = "Listar endereços do usuário",
            description = "Retorna todos os endereços ativos do usuário autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AddressResponseDTO.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses() {
        List<AddressResponseDTO> addresses = addressService.getUserAddresses();
        return ResponseEntity.ok(addresses);
    }

    @Operation(
            summary = "Buscar endereço por ID",
            description = "Busca um endereço específico do usuário autenticado pelo seu ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable Long id) {
        AddressResponseDTO address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

    @Operation(
            summary = "Buscar endereço principal",
            description = "Retorna o endereço principal do usuário autenticado."
    )
    @GetMapping("/principal")
    public ResponseEntity<AddressResponseDTO> getPrincipalAddress() {
        AddressResponseDTO address = addressService.getPrincipalAddress();
        return ResponseEntity.ok(address);
    }

    // ========== CRIAÇÃO ==========

    @Operation(
            summary = "Criar novo endereço",
            description = "Cria um novo endereço para o usuário autenticado.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso",
                            content = @Content(schema = @Schema(implementation = AddressResponseDTO.class)))
            }
    )
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== ATUALIZAÇÃO ==========

    @Operation(
            summary = "Atualizar endereço existente",
            description = "Atualiza um endereço existente do usuário autenticado."
    )
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Definir endereço principal",
            description = "Define um endereço como principal para o usuário autenticado."
    )
    @PatchMapping("/{id}/principal")
    public ResponseEntity<AddressResponseDTO> setAsPrincipal(@PathVariable Long id) {
        AddressResponseDTO response = addressService.setAsPrincipal(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Restaurar endereço deletado",
            description = "Restaura um endereço que foi previamente deletado (soft delete)."
    )
    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<AddressResponseDTO> restoreAddress(@PathVariable Long id) {
        AddressResponseDTO response = addressService.restoreAddress(id);
        return ResponseEntity.ok(response);
    }

    // ========== DELEÇÃO ==========

    @Operation(
            summary = "Deletar endereço",
            description = "Realiza o soft delete de um endereço do usuário autenticado."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    // ========== ESTATÍSTICAS ==========

    @Operation(
            summary = "Contar endereços ativos",
            description = "Retorna a quantidade de endereços ativos do usuário."
    )
    @GetMapping("/stats/count")
    public ResponseEntity<Long> countActiveAddresses() {
        long count = addressService.countUserActiveAddresses();
        return ResponseEntity.ok(count);
    }

    @Operation(
            summary = "Verificar se há endereços cadastrados",
            description = "Verifica se o usuário possui algum endereço cadastrado."
    )
    @GetMapping("/stats/has-addresses")
    public ResponseEntity<Boolean> hasAddresses() {
        boolean hasAddresses = addressService.hasAddresses();
        return ResponseEntity.ok(hasAddresses);
    }

    @Operation(
            summary = "Verificar se há endereço principal",
            description = "Verifica se o usuário possui um endereço principal definido."
    )
    @GetMapping("/stats/has-principal")
    public ResponseEntity<Boolean> hasPrincipalAddress() {
        boolean hasPrincipal = addressService.hasPrincipalAddress();
        return ResponseEntity.ok(hasPrincipal);
    }
}
