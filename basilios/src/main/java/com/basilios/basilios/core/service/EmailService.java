package com.basilios.basilios.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token, String userName) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperação de Senha - Basilios");
            message.setText(buildEmailContent(userName, resetLink));

            mailSender.send(message);
            log.info("Email de recuperação enviado para: {}", toEmail);
        } catch (Exception e) {
            log.error("Erro ao enviar email de recuperação para: {}", toEmail, e);
            throw new RuntimeException("Erro ao enviar email de recuperação", e);
        }
    }

    private String buildEmailContent(String userName, String resetLink) {
        return String.format(
                "Olá %s,\n\n" +
                        "Recebemos uma solicitação para redefinir sua senha.\n\n" +
                        "Clique no link abaixo para criar uma nova senha:\n" +
                        "%s\n\n" +
                        "Este link é válido por 1 hora.\n\n" +
                        "Se você não solicitou a redefinição de senha, ignore este email.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe Basilios",
                userName,
                resetLink
        );
    }
}