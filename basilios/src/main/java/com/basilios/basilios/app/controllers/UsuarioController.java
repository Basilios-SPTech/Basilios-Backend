package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.core.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Operation(summary = "Atualizar parcialmente usuário", description = "Atualiza dados permitidos do usuário via PATCH")
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioProfileResponse> updateUserPatch(@PathVariable Long id, @Valid @RequestBody UsuarioProfileResponse dto) {
        UsuarioProfileResponse updated = usuarioService.updateUsuarioPatch(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna todos os usuários cadastrados")
    @GetMapping
    public ResponseEntity<List<UsuarioListarDTO>> getAllUsers() {
        List<UsuarioListarDTO> usuarios = usuarioService.findAll().stream()
            .map(u -> {
                UsuarioListarDTO dto = new UsuarioListarDTO();
                dto.setNomeUsuario(u.getNomeUsuario());
                dto.setEmail(u.getEmail());
                dto.setCpf(u.getCpf());
                dto.setTelefone(u.getTelefone());
                dto.setDataNascimento(u.getDataNascimento());
                return dto;
            })
            .toList();
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Buscar usuário por ID", description = "Retorna detalhes do usuário pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioProfileResponse> getUserById(@PathVariable Long id) {
        var usuario = usuarioService.findById(id);
        UsuarioProfileResponse dto = UsuarioProfileResponse.builder()
            .id(usuario.getId())
            .nomeUsuario(usuario.getNomeUsuario())
            .email(usuario.getEmail())
            .cpf(usuario.getCpf())
            .telefone(usuario.getTelefone())
            .dataNascimento(usuario.getDataNascimento())
            .roles(new java.util.HashSet<>(usuario.getRoles()))
            .enabled(usuario.isAtivo())
            .createdAt(usuario.getCreatedAt())
            .build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Soft delete de usuário", description = "Desativa o usuário (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<UsuarioListarDTO> deleteUser(@PathVariable Long id) {
        UsuarioListarDTO dto = usuarioService.deleteUsuario(id);
        return ResponseEntity.ok(dto);
    }
}
