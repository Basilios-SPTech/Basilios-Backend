package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.PasswordReset;
import com.basilios.basilios.core.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetServiceTest {

    @Test
    @DisplayName("Deve criar um PasswordReset com código, expiração e usuário")
    void shouldCreatePasswordResetSuccessfully() {
        // Arrange
        Usuario user = new Usuario();
        user.setEmail("user@test.com");

        String codigo = "123456";
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(30);

        // Act
        PasswordReset reset = new PasswordReset(codigo, expiracao, user);

        // Assert
        assertEquals(codigo, reset.getCodigo());
        assertEquals(expiracao, reset.getExpiracao());
        assertEquals(user, reset.getUsuario());
    }

    @Test
    @DisplayName("Deve verificar que código expirado é detectável via isExpirado()")
    void shouldDetectExpiredCode() {
        // Arrange
        Usuario user = new Usuario();
        user.setEmail("user@test.com");

        LocalDateTime expiracao = LocalDateTime.now().minusMinutes(5);
        PasswordReset reset = new PasswordReset("expired-code", expiracao, user);

        // Assert
        assertTrue(reset.isExpirado(), "Código expirado deve retornar true em isExpirado()");
    }

    @Test
    @DisplayName("Deve verificar que código válido não está expirado via isExpirado()")
    void shouldDetectValidCode() {
        // Arrange
        Usuario user = new Usuario();
        user.setEmail("user@test.com");

        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(30);
        PasswordReset reset = new PasswordReset("valid-code", expiracao, user);

        // Assert
        assertFalse(reset.isExpirado(), "Código válido deve retornar false em isExpirado()");
    }
}
