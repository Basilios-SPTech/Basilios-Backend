package com.basilios.basilios.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Redefinição de Senha");
            message.setText(buildEmailContent(resetLink));

            mailSender.send(message);
            log.info("Email de redefinição de senha enviado para: {}", to);
        } catch (Exception e) {
            log.error("Erro ao enviar email para: {}", to, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    private String buildEmailContent(String resetLink) {
        return "Olá,\n\n" +
                "Você solicitou a redefinição de senha.\n\n" +
                "Clique no link abaixo para redefinir sua senha:\n\n" +
                resetLink + "\n\n" +
                "Este link é válido por 1 hora.\n\n" +
                "Se você não solicitou esta redefinição, ignore este email.\n\n" +
                "Atenciosamente,\n" +
                "Equipe de Suporte";
    }
}