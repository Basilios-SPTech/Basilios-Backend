package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.AuthenticationException;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import com.basilios.basilios.infra.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Registra um novo usuário (cliente por padrão)
     */
    @Transactional
    public UsuarioTokenDTO register(UsuarioRegisterDTO request) {
        String cpfNormalizado = normalizarCpf(request.getCpf());
        String telefoneNormalizado = normalizarTelefone(request.getTelefone());

        // Valida duplicidade
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        if (usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new BusinessException("CPF já cadastrado");
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
                .build();

        usuario = usuarioRepository.save(usuario);

        // Gera o token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return UsuarioTokenDTO.builder()
                .token(token)
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .build();
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
            throw new AuthenticationException("Credenciais inválidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Usuário não encontrado"));

        if (!usuario.getEnabled()) {
            throw new AuthenticationException("Usuário desativado");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return UsuarioTokenDTO.builder()
                .token(token)
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .build();
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? null : telefone.replaceAll("[^0-9]", "");
    }
}
