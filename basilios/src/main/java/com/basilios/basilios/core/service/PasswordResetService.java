package com.basilios.basilios.core.service;

import com.basilios.basilios.core.model.PasswordResetToken;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.PasswordResetTokenRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void createPasswordResetToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Deletar tokens antigos do usuário
        tokenRepository.deleteByUsuario(usuario);

        // Criar novo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Expira em 1 hora

        tokenRepository.save(resetToken);

        // Enviar email
        sendResetEmail(usuario.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // Deletar token usado
        tokenRepository.delete(resetToken);
    }

    private void sendResetEmail(String email, String token) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Solicitação de Reset de Senha - Basilios");
        message.setText("Olá,\n\n"
                + "Você solicitou o reset de sua senha.\n\n"
                + "Clique no link abaixo para criar uma nova senha:\n"
                + resetUrl + "\n\n"
                + "Este link expira em 1 hora.\n\n"
                + "Se você não solicitou esta alteração, ignore este email.");

        mailSender.send(message);
    }
}