package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.reset.EmailDTO;
import com.basilios.basilios.app.dto.reset.ResetSenhaDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar novo cliente", description = "Cria uma nova conta de cliente e retorna token JWT")
    @PostMapping("/register")
    public ResponseEntity<UsuarioTokenDTO> register(@Valid @RequestBody UsuarioRegisterDTO request) {
        UsuarioTokenDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login", description = "Autentica com email/nomeUsuario e senha, retorna token JWT")
    @PostMapping("/login")
    public ResponseEntity<UsuarioTokenDTO> login(@Valid @RequestBody UsuarioLoginDTO request) {
        UsuarioTokenDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Dispara o fluxo de recuperação de senha sem expor se o email existe.
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailDTO request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Se o email estiver cadastrado, voce recebera as instrucoes de redefinicao."
        ));
    }

    /**
     * Aplica nova senha a partir de token de recuperação.
     */
    @PostMapping("/reset-senha")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetSenhaDTO request) {
        authService.resetPassword(request.getCodigo(), request.getNovaSenha());
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }
}