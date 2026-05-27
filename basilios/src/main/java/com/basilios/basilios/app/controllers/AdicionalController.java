package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.adicional.AdicionalRequestDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalResponseDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalUpdateDTO;
import com.basilios.basilios.core.service.AdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adicionais")
@RequiredArgsConstructor
@Tag(name = "Adicionais", description = "CRUD de adicionais para produtos")
public class AdicionalController {

    private final AdicionalService adicionalService;

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PostMapping
    @Operation(summary = "Criar adicional", description = "Cria um novo adicional (ROLE_FUNCIONARIO)")
    public ResponseEntity<AdicionalResponseDTO> create(
            @Valid @RequestBody AdicionalRequestDTO dto) {
        AdicionalResponseDTO created = adicionalService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar adicionais", description = "Lista todos os adicionais ativos")
    public ResponseEntity<Page<AdicionalResponseDTO>> listAll(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdicionalResponseDTO> page = adicionalService.listAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar adicional por ID", description = "Retorna um adicional pelo ID")
    public ResponseEntity<AdicionalResponseDTO> getById(@PathVariable Long id) {
        AdicionalResponseDTO dto = adicionalService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar adicional", description = "Atualiza dados do adicional (ROLE_FUNCIONARIO)")
    public ResponseEntity<AdicionalResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AdicionalUpdateDTO dto) {
        AdicionalResponseDTO updated = adicionalService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('FUNCIONARIO')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar adicional", description = "Soft delete do adicional (ROLE_FUNCIONARIO)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adicionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
