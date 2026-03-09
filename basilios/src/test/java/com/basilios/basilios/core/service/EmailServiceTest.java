package com.basilios.basilios.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    @DisplayName("Deve enviar o email de redefinição de senha com sucesso")
    void shouldSendPasswordResetEmailSuccessfully() {
        // Arrange
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        String to = "user@test.com";
        String link = "https://example.com/reset/123";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendPasswordResetEmail(to, link);

        // Assert
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals("Redefinição de Senha", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(link));
        assertTrue(sentMessage.getText().contains("Você solicitou a redefinição de senha"));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o envio de email falhar")
    void shouldThrowRuntimeExceptionWhenEmailFails() {
        // Arrange
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        String to = "user@test.com";
        String link = "https://example.com/reset/abc";

        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendPasswordResetEmail(to, link));

        assertEquals("Erro ao enviar email", exception.getMessage());
    }
}
