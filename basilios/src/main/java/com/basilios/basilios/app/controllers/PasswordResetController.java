package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.reset.PasswordResetDTOs;
import com.basilios.basilios.core.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<PasswordResetDTOs.Response> requestPasswordReset(
            @Valid @RequestBody PasswordResetDTOs.Request request) {
        try {
            passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(
                    new PasswordResetDTOs.Response("Email de redefinição enviado com sucesso. Verifique sua caixa de entrada.")
            );
        } catch (Exception e) {
            log.error("Erro ao processar solicitação de reset de senha", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetDTOs.Response("Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetDTOs.Response> resetPassword(
            @Valid @RequestBody PasswordResetDTOs.Reset resetDTO) {
        try {
            passwordResetService.resetPassword(resetDTO);
            return ResponseEntity.ok(
                    new PasswordResetDTOs.Response("Senha alterada com sucesso! Você já pode fazer login.")
            );
        } catch (Exception e) {
            log.error("Erro ao redefinir senha", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetDTOs.Response("Erro: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<PasswordResetDTOs.Response> validateToken(
            @RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateToken(token);
            if (isValid) {
                return ResponseEntity.ok(
                        new PasswordResetDTOs.Response("Token válido")
                );
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new PasswordResetDTOs.Response("Token inválido ou expirado"));
            }
        } catch (Exception e) {
            log.error("Erro ao validar token", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetDTOs.Response("Erro ao validar token"));
        }
    }
}