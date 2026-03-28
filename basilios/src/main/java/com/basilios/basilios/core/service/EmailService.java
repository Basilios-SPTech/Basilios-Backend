package com.basilios.basilios.core.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    // ========== RESET DE SENHA ==========

    public void sendPasswordResetEmail(String to, String resetLink) {
        sendPasswordResetEmail(to, resetLink, "", "1 hora", "tel:+551148014864");
    }

    // Sobrecarga para manter compatibilidade de testes e futuras variacoes de template.
    public void sendPasswordResetEmail(String to, String resetLink, String userName, String expiresIn, String supportEmailValue) {
        try {
            String textBody = buildPasswordResetContent(resetLink);
            String htmlBody = buildPasswordResetHtml(resetLink, userName, expiresIn, supportEmailValue);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("Redefinição de Senha - Basilios");
            helper.setText(textBody, htmlBody);

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

    private String buildPasswordResetHtml(String resetUrl, String userName, String expiresIn, String supportEmailValue) {
        String safeName = (userName == null || userName.isBlank()) ? "" : " " + userName;
        String safeExpires = (expiresIn == null || expiresIn.isBlank()) ? "1 hora" : expiresIn;

        String template = """
                <!doctype html>
                <html lang="pt-BR">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="x-apple-disable-message-reformatting" />
                    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
                    <title>Redefinição de senha - Basilios</title>
                  </head>
                  <body style="margin:0;padding:0;background:#f5f5f5;">
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:24px 12px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:100%;max-width:600px;background:#ffffff;border:1px solid #e5e7eb;border-radius:14px;overflow:hidden;font-family:Arial,Helvetica,sans-serif;">
                            <tr>
                              <td style="padding:28px 24px 10px 24px;color:#111111;">
                                <h1 style="margin:0 0 10px 0;font-size:24px;line-height:1.3;">Redefinição de senha</h1>
                                <p style="margin:0 0 14px 0;font-size:15px;line-height:1.6;color:#374151;">Olá{{userName}},</p>
                                <p style="margin:0 0 18px 0;font-size:15px;line-height:1.6;color:#374151;">Recebemos uma solicitação para redefinir a senha da sua conta Basilios.</p>
                                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin:20px 0 22px 0;">
                                  <tr><td align="center"><a href="{{resetUrl}}" style="display:inline-block;background:#BB3530;color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;line-height:1;padding:14px 24px;border-radius:10px;">Redefinir senha</a></td></tr>
                                </table>
                                <p style="margin:0 0 8px 0;font-size:13px;line-height:1.6;color:#6b7280;">Este link expira em <strong style="color:#111111;">{{expiresIn}}</strong>.</p>
                                <p style="margin:0 0 8px 0;font-size:13px;line-height:1.6;color:#6b7280;">Se o botão não funcionar, copie e cole este link no navegador:</p>
                                <p style="margin:0 0 18px 0;word-break:break-all;"><a href="{{resetUrl}}" style="font-size:13px;color:#BB3530;text-decoration:underline;">{{resetUrl}}</a></p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:0 24px 20px 24px;">
                                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#fff7f7;border:1px solid #f2d2d0;border-radius:10px;">
                                  <tr><td style="padding:12px 14px;font-size:12px;line-height:1.6;color:#7f1d1d;">Se você não solicitou esta redefinição, pode ignorar este e-mail com segurança.</td></tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="border-top:1px solid #e5e7eb;padding:16px 24px 24px 24px;color:#6b7280;font-size:12px;line-height:1.6;">Atenciosamente,<br /><strong style="color:#111111;">Equipe Basilios</strong><br /><a href="tel:+551148014864" style="color:#BB3530;text-decoration:none;">(11) 4801-4864</a></td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """;

        return template
                .replace("{{userName}}", safeName)
                .replace("{{resetUrl}}", resetUrl)
                .replace("{{expiresIn}}", safeExpires);
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
            message.setFrom(mailFrom);
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