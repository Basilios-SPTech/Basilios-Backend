package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UpdateRoleRequest;
import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.app.dto.user.UsuarioRequestPatch;
import com.basilios.basilios.app.mapper.UsuarioMapper;
import com.basilios.basilios.core.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Atualizar parcialmente usuário", description = "Atualiza dados permitidos do usuário via PATCH")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO') or @usuarioService.getCurrentUsuario().id == #id")
    public ResponseEntity<UsuarioProfileResponse> updateUserPatch(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestPatch dto
    ) {
        UsuarioProfileResponse updated = usuarioService.updateUsuarioPatch(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Atualizar roles do usuário", description = "Atualiza os perfis (roles) do usuário")
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioProfileResponse> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(usuarioService.updateRole(id, request.getRoles()));
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna os usuários cadastrados com paginação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO')")
    public ResponseEntity<Page<UsuarioListarDTO>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UsuarioListarDTO> usuarios = usuarioService.findAll(pageable)
                .map(UsuarioMapper::toListarDTO);
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna detalhes do usuário pelo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO') or @usuarioService.getCurrentUsuario().id == #id")
    public ResponseEntity<UsuarioProfileResponse> getUserById(@PathVariable Long id) {
        var usuario = usuarioService.findById(id);
        return ResponseEntity.ok(UsuarioMapper.toProfileResponse(usuario));
    }

    @Operation(summary = "Soft delete de usuário", description = "Desativa o usuário (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FUNCIONARIO')")
    public ResponseEntity<UsuarioListarDTO> deleteUser(@PathVariable Long id) {
        UsuarioListarDTO dto = usuarioService.deleteUsuario(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Buscar dados do usuário autenticado", description = "Retorna os dados do usuário logado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioProfileResponse> getMe() {
        var usuario = usuarioService.getCurrentUsuario();
        return ResponseEntity.ok(UsuarioMapper.toProfileResponse(usuario));
    }
}