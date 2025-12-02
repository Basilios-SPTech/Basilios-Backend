package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.user.MessageResponseDTO;
import com.basilios.basilios.app.dto.user.PasswordResetDTO;
import com.basilios.basilios.app.dto.user.PasswordResetRequestDTO;
import com.basilios.basilios.core.exception.TokenExpiredException;
import com.basilios.basilios.core.exception.TokenNotFoundException;
import com.basilios.basilios.core.exception.UserNotFoundException;
import com.basilios.basilios.core.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor

public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<MessageResponseDTO> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO request) {
        try {
            passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(new MessageResponseDTO(
                    "Se o email estiver cadastrado, você receberá um link de recuperação."
            ));
        } catch (UserNotFoundException e) {
            // Por segurança, não revelamos se o email existe ou não
            return ResponseEntity.ok(new MessageResponseDTO(
                    "Se o email estiver cadastrado, você receberá um link de recuperação."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Erro ao processar solicitação. Tente novamente mais tarde."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        try {
            passwordResetService.resetPassword(resetDTO);
            return ResponseEntity.ok(new MessageResponseDTO("Senha redefinida com sucesso!"));
        } catch (TokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Erro ao redefinir senha. Tente novamente mais tarde."));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<MessageResponseDTO> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok(new MessageResponseDTO("Token válido"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO("Token inválido ou expirado"));
        }
    }
}