package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.PasswordResetDTO;
import com.basilios.basilios.app.dto.user.PasswordResetRequestDTO;
import com.basilios.basilios.app.dto.user.PasswordResetDTO;
import com.basilios.basilios.app.dto.user.PasswordResetRequestDTO;
import com.basilios.basilios.core.exception.TokenExpiredException;
import com.basilios.basilios.core.exception.TokenNotFoundException;
import com.basilios.basilios.core.exception.UserNotFoundException;
import com.basilios.basilios.core.model.PasswordResetToken;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.core.repository.PasswordResetTokenRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRATION_HOURS = 1;

    @Transactional
    public void requestPasswordReset(@Valid PasswordResetRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o email: " + request.getEmail()));

        // Remove tokens antigos do usuário
        tokenRepository.deleteByUser(usuario);

        // Cria novo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(usuario)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Envia email
        emailService.sendPasswordResetEmail(usuario.getEmail(), token, usuario.getNomeUsuario());

        log.info("Token de reset criado para o usuário: {}", usuario.getEmail());
    }

    @Transactional
    public void resetPassword(@Valid PasswordResetDTO resetDTO) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(resetDTO.getToken())
                .orElseThrow(() -> new TokenNotFoundException("Token inválido ou já utilizado"));

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Token expirado. Solicite um novo link de recuperação.");
        }

        Usuario usuario = resetToken.getUser();
        usuario.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        usuarioRepository.save(usuario);

        // Marca token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Senha redefinida com sucesso para o usuário: {}", usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenRepository.findByTokenAndUsedFalse(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    // Limpa tokens expirados a cada dia às 3h da manhã
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Tokens expirados removidos");
    }
}