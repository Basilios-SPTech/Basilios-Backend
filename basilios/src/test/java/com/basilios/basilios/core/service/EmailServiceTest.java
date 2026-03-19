package com.basilios.basilios.core.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    // ========== TESTES DE NOTIFICAÇÕES DE PEDIDO ==========

    @Test
    @DisplayName("Deve enviar email de pedido confirmado com sucesso")
    void shouldSendOrderConfirmedEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderConfirmedEmail("cliente@test.com", "João", "PED-123");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertEquals("cliente@test.com", msg.getTo()[0]);
        assertTrue(msg.getSubject().contains("PED-123"));
        assertTrue(msg.getText().contains("João"));
    }

    @Test
    @DisplayName("Deve enviar email de pedido em preparo com sucesso")
    void shouldSendOrderPreparingEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderPreparingEmail("cliente@test.com", "Maria", "PED-456");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertEquals("cliente@test.com", msg.getTo()[0]);
        assertTrue(msg.getSubject().contains("PED-456"));
        assertTrue(msg.getText().contains("Maria"));
    }

    @Test
    @DisplayName("Deve enviar email de pedido despachado com sucesso")
    void shouldSendOrderDispatchedEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderDispatchedEmail("cliente@test.com", "Pedro", "PED-789");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertTrue(msg.getSubject().contains("entrega"));
        assertTrue(msg.getText().contains("Pedro"));
    }

    @Test
    @DisplayName("Deve enviar email de pedido entregue com sucesso")
    void shouldSendOrderDeliveredEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderDeliveredEmail("cliente@test.com", "Ana", "PED-101");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertTrue(msg.getSubject().contains("entregue"));
        assertTrue(msg.getText().contains("Ana"));
    }

    @Test
    @DisplayName("Deve enviar email de pedido cancelado com motivo")
    void shouldSendOrderCancelledEmailWithReason() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderCancelledEmail("cliente@test.com", "Carlos", "PED-202", "Fora da área");

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertTrue(msg.getSubject().contains("cancelado"));
        assertTrue(msg.getText().contains("Carlos"));
        assertTrue(msg.getText().contains("Fora da área"));
    }

    @Test
    @DisplayName("Deve enviar email de pedido cancelado sem motivo")
    void shouldSendOrderCancelledEmailWithoutReason() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOrderCancelledEmail("cliente@test.com", "Carlos", "PED-303", null);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertTrue(msg.getText().contains("Motivo não informado"));
    }
}
