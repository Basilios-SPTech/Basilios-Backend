package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.AuthenticationException;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.model.PasswordReset;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.PasswordResetRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import com.basilios.basilios.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_BYTES = 32;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${app.password-reset.ttl-minutes:60}")
    private long passwordResetTtlMinutes;

    @Value("${app.password-reset.base-url:http://localhost:3000/reset-password?token=}")
    private String passwordResetBaseUrl;

    /**
     * Registra um novo usuário (cliente por padrão)
     */
    @Transactional
    public UsuarioTokenDTO register(UsuarioRegisterDTO request) {
        String cpfNormalizado = normalizarCpf(request.getCpf());
        String telefoneNormalizado = normalizarTelefone(request.getTelefone());

        // Valida duplicidade
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email ja cadastrado");
        }
        if (usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new BusinessException("CPF ja cadastrado");
        }

        // Cria o novo usuário
        Usuario usuario = Usuario.builder()
                .nomeUsuario(request.getNomeUsuario())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(cpfNormalizado)
                .telefone(telefoneNormalizado)
                .roles(List.of(RoleEnum.ROLE_CLIENTE)) // padrão
                .enabled(true)
                .dataNascimento(request.getDataNascimento())
                .build();

        usuario = usuarioRepository.save(usuario);

        return buildTokenResponse(usuario);
    }

    /**
     * Autentica um usuário e retorna o token JWT
     */
    public UsuarioTokenDTO login(UsuarioLoginDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Credenciais invalidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Usuario nao encontrado"));

        if (!usuario.getEnabled()) {
            throw new AuthenticationException("Usuario desativado");
        }

        return buildTokenResponse(usuario);
    }

    private UsuarioTokenDTO buildTokenResponse(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Enum::name)
                .toList();
        String token = jwtUtil.generateToken(
                userDetailsService.loadUserByUsername(usuario.getEmail()).getUsername(),
                roles,
                usuario.getId()
        );
        return UsuarioTokenDTO.builder()
                .token(token)
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .roles(roles)
                .build();
    }

    /**
     * Inicia o reset sem revelar se o email existe (anti-enumeracao).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        passwordResetRepository.deleteByExpiracaoBefore(LocalDateTime.now());

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            passwordResetRepository.deleteByUsuarioId(usuario.getId());

            String rawToken = generateToken();
            String tokenHash = sha256(rawToken);
            LocalDateTime expiracao = LocalDateTime.now().plusMinutes(passwordResetTtlMinutes);

            PasswordReset reset = new PasswordReset(tokenHash, expiracao, usuario);
            passwordResetRepository.save(reset);

            try {
                emailService.sendPasswordResetEmail(usuario.getEmail(), buildResetLink(rawToken));
            } catch (RuntimeException ex) {
                // Nao vaza erro de infraestrutura para o cliente no endpoint de recuperacao.
                org.slf4j.LoggerFactory.getLogger(AuthService.class)
                        .error("Falha ao enviar email de reset para usuarioId={}", usuario.getId(), ex);
            }
        });
    }

    /**
     * Conclui o reset com token de uso unico e valida expiracao.
     */
    @Transactional
    public void resetPassword(String rawToken, String novaSenha) {
        String tokenHash = sha256(rawToken);

        PasswordReset reset = passwordResetRepository.findByCodigo(tokenHash)
                .orElseThrow(() -> new BusinessException("Codigo de redefinicao invalido ou expirado"));

        if (reset.getExpiracao().isBefore(LocalDateTime.now())) {
            passwordResetRepository.delete(reset);
            throw new BusinessException("Codigo de redefinicao invalido ou expirado");
        }

        Usuario usuario = reset.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        passwordResetRepository.deleteByUsuarioId(usuario.getId());
    }

    private String buildResetLink(String rawToken) {
        if (passwordResetBaseUrl.contains("{token}")) {
            return passwordResetBaseUrl.replace("{token}", rawToken);
        }
        return passwordResetBaseUrl + rawToken;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nao disponivel", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? null : telefone.replaceAll("[^0-9]", "");
    }
}
