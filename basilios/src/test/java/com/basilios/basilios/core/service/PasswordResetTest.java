package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.reset.PasswordResetDTOs;
import com.basilios.basilios.core.model.PasswordResetToken;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.PasswordResetTokenRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final UsuarioRepository userRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final EmailService emailService = mock(EmailService.class);

    private final PasswordResetService passwordResetService =
            new PasswordResetService(tokenRepository, userRepository, passwordEncoder, emailService);

    @Test
    @DisplayName("Deve solicitar o reset de senha com sucesso quando o email existir")
    void shouldRequestPasswordResetSuccessfully() {
        // Arrange
        String email = "user@test.com";
        PasswordResetDTOs.Request requestDTO = new PasswordResetDTOs.Request(email);

        Usuario user = new Usuario();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        // Captura token salvo
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        // Act
        passwordResetService.requestPasswordReset(requestDTO);

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, times(1)).deleteByUser(user);
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());
        verify(emailService, times(1))
                .sendPasswordResetEmail(eq(email), contains("/reset-password?token="));

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertEquals(user, savedToken.getUser());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o email não existir no sistema")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Arrange
        String email = "notfound@test.com";
        PasswordResetDTOs.Request requestDTO = new PasswordResetDTOs.Request(email);

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> passwordResetService.requestPasswordReset(requestDTO));

        assertEquals("Usuário não encontrado com o email: " + email, ex.getMessage());

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }
}
