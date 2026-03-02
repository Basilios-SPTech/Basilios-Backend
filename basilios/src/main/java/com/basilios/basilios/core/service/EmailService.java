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

    // ========== RESET DE SENHA ==========

    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Redefinição de Senha - Basilios");
            message.setText(buildPasswordResetContent(resetLink));

            mailSender.send(message);
            log.info("Email de redefinição de senha enviado para: {}", to);
        } catch (Exception e) {
            log.error("Erro ao enviar email para: {}", to, e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    private String buildPasswordResetContent(String resetLink) {
        return "Olá,\n\n" +
                "Você solicitou a redefinição de senha.\n\n" +
                "Clique no link abaixo para redefinir sua senha:\n\n" +
                resetLink + "\n\n" +
                "Este link é válido por 1 hora.\n\n" +
                "Se você não solicitou esta redefinição, ignore este email.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Basilios";
    }

    // ========== NOTIFICAÇÕES DE PEDIDO ==========

    /**
     * Email enviado quando pedido é confirmado pelo estabelecimento
     */
    public void sendOrderConfirmedEmail(String to, String clientName, String orderCode) {
        String subject = "Pedido Confirmado! - " + orderCode;
        String content = String.format(
                "Olá %s! 👋\n\n" +
                "Ótima notícia! Seu pedido %s foi confirmado!\n\n" +
                "Estamos preparando tudo com muito carinho. 🍔\n\n" +
                "Você receberá uma notificação assim que começarmos a preparar.\n\n" +
                "Obrigado pela preferência!\n\n" +
                "Equipe Basilios",
                clientName, orderCode
        );
        sendEmail(to, subject, content);
    }

    /**
     * Email enviado quando pedido começa a ser preparado
     */
    public void sendOrderPreparingEmail(String to, String clientName, String orderCode) {
        String subject = "Seu pedido está sendo preparado! - " + orderCode;
        String content = String.format(
                "Olá %s! 👨‍🍳\n\n" +
                "Seu pedido %s está sendo preparado agora!\n\n" +
                "Nossa equipe já está na cozinha preparando seu lanche com todo cuidado.\n\n" +
                "Em breve ele sairá para entrega!\n\n" +
                "Equipe Basilios",
                clientName, orderCode
        );
        sendEmail(to, subject, content);
    }

    /**
     * Email enviado quando pedido sai para entrega
     */
    public void sendOrderDispatchedEmail(String to, String clientName, String orderCode) {
        String subject = "Seu pedido saiu para entrega! 🛵 - " + orderCode;
        String content = String.format(
                "Olá %s! 🛵\n\n" +
                "Seu pedido %s acabou de sair para entrega!\n\n" +
                "Nosso entregador está a caminho.\n" +
                "Fique atento, logo logo ele chega!\n\n" +
                "Bom apetite!\n\n" +
                "Equipe Basilios",
                clientName, orderCode
        );
        sendEmail(to, subject, content);
    }

    /**
     * Email enviado quando pedido é entregue
     */
    public void sendOrderDeliveredEmail(String to, String clientName, String orderCode) {
        String subject = "Pedido entregue! Bom apetite! 🍔 - " + orderCode;
        String content = String.format(
                "Olá %s! 🎉\n\n" +
                "Seu pedido %s foi entregue com sucesso!\n\n" +
                "Esperamos que você aproveite bastante! 🍔🍟\n\n" +
                "Se puder, deixe sua avaliação. Sua opinião é muito importante para nós!\n\n" +
                "Obrigado pela preferência e até a próxima!\n\n" +
                "Equipe Basilios",
                clientName, orderCode
        );
        sendEmail(to, subject, content);
    }

    /**
     * Email enviado quando pedido é cancelado
     */
    public void sendOrderCancelledEmail(String to, String clientName, String orderCode, String motivo) {
        String subject = "Pedido cancelado - " + orderCode;
        String motivoText = motivo != null && !motivo.isBlank() 
                ? "Motivo: " + motivo 
                : "Motivo não informado.";
        
        String content = String.format(
                "Olá %s,\n\n" +
                "Infelizmente seu pedido %s foi cancelado.\n\n" +
                "%s\n\n" +
                "Se você não solicitou o cancelamento ou tem alguma dúvida, " +
                "entre em contato conosco.\n\n" +
                "Pedimos desculpas pelo inconveniente.\n\n" +
                "Equipe Basilios",
                clientName, orderCode, motivoText
        );
        sendEmail(to, subject, content);
    }

    // ========== MÉTODO AUXILIAR ==========

    /**
     * Método genérico para envio de emails
     */
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email enviado para: {} - Assunto: {}", to, subject);
        } catch (Exception e) {
            log.error("Erro ao enviar email para: {} - Assunto: {}", to, subject, e);
            throw new RuntimeException("Erro ao enviar email: " + e.getMessage(), e);
        }
    }
}