package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.core.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Retorna perfil do usuário autenticado
     * GET /api/usuarios/me
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioProfileResponse> getProfile() {
        UsuarioProfileResponse profile = usuarioService.getProfile();
        return ResponseEntity.ok(profile);
    }
}