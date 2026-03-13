package com.basilios.basilios.core.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    @DisplayName("Deve enviar o email de redefinicao de senha com sucesso")
    void shouldSendPasswordResetEmailSuccessfully() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@basilios.com");

        String to = "user@test.com";
        String link = "https://example.com/reset/123";

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail(to, link, "Rafael", "1 hora", "127.0.0.1");

        verify(mailSender, times(1)).send(mimeMessage);
        assertEquals("Redefinição de Senha - Basilios", mimeMessage.getSubject());
    }

    @Test
    @DisplayName("Deve lancar RuntimeException quando o envio de email falhar")
    void shouldThrowRuntimeExceptionWhenEmailFails() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);

        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@basilios.com");

        String to = "user@test.com";
        String link = "https://example.com/reset/abc";

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendPasswordResetEmail(to, link, "Rafael", "1 hora", ""));

        assertEquals("Erro ao enviar email", exception.getMessage());
    }
}
