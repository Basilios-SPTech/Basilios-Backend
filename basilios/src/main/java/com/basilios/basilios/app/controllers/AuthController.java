package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.core.service.AuthService;
import com.basilios.basilios.core.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Registra novo cliente
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioTokenDTO> register(@Valid @RequestBody UsuarioRegisterDTO request) {
        UsuarioTokenDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login com email ou nomeUsuario
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioTokenDTO> login(@Valid @RequestBody UsuarioLoginDTO request) {
        UsuarioTokenDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public Usuario me() {
         Usuario usuario = usuarioService.getCurrentUsuario();
         return usuario;
    }
}