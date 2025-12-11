package com.basilios.basilios.core.service;



import com.basilios.basilios.app.dto.reset.PasswordResetDTOs;
import com.basilios.basilios.core.model.PasswordResetToken;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.PasswordResetTokenRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${http://localhost:5173/}")
    private String frontendUrl;

    @Transactional
    public void requestPasswordReset(PasswordResetDTOs.Request request) {
        Usuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o email: " + request.getEmail()));

        // Remove tokens antigos do usuário
        tokenRepository.deleteByUser(user);

        // Gera novo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        // Envia email com o link de reset
        String resetLink = frontendUrl + "/ls ?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Token de reset de senha gerado para o usuário: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(PasswordResetDTOs.Reset resetDTO) {
        PasswordResetToken resetToken = tokenRepository.findByToken(resetDTO.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token já foi utilizado");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado. Solicite um novo link de redefinição");
        }

        // Atualiza a senha do usuário
        Usuario user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        userRepository.save(user);

        // Marca o token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Senha redefinida com sucesso para o usuário: {}", user.getEmail());
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsed() && !t.isExpired())
                .orElse(false);
    }
}