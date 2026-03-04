package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.reset.EmailDTO;
import com.basilios.basilios.app.dto.reset.JwtResponse;
import com.basilios.basilios.app.dto.reset.ResetSenhaDTO;
import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.PasswordReset;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.core.service.AuthService;
import com.basilios.basilios.core.service.EmailService;
import com.basilios.basilios.core.service.UsuarioService;
import com.basilios.basilios.infra.repository.PasswordResetRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import com.basilios.basilios.infra.security.JwtUtil;
import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


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

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String codigo = UUID.randomUUID().toString();

        PasswordReset reset = new PasswordReset();
        reset.setCodigo(codigo);
        reset.setUsuario(usuario);
        reset.setExpiracao(LocalDateTime.now().plusHours(1));

        passwordResetRepository.save(reset);

// 🚨 TESTE TEMPORÁRIO
// emailService.sendPasswordResetEmail(usuario.getEmail(), codigo);
        System.out.println("Código reset: " + codigo);

        return ResponseEntity.ok("Email enviado (modo teste)");
//
//        passwordResetRepository.save(reset);
//
//        emailService.sendPasswordResetEmail(usuario.getEmail(), codigo);
//
//        return ResponseEntity.ok("Email enviado");
    }

    @PostMapping("/reset-senha")
    public ResponseEntity<?> resetPassword(@RequestBody ResetSenhaDTO dto) {

        PasswordReset reset = passwordResetRepository.findByCodigo(dto.getCodigo())
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (reset.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        Usuario usuario = reset.getUsuario();

        // atualiza senha
        usuario.setPassword(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);

        // remove reset
        passwordResetRepository.delete(reset);

        // 🚀 USA SEU JwtUtil
        String token = jwtUtil.generateToken(usuario.getEmail());

        return ResponseEntity.ok(new JwtResponse(token) {
        });
    }
}