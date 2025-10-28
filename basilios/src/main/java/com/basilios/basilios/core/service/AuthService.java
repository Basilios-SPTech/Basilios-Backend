package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.auth.AuthResponseDTO;
import com.basilios.basilios.app.dto.auth.LoginRequestDTO;
import com.basilios.basilios.app.dto.auth.RegisterRequestDTO;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.AuthenticationException;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.model.Client;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.ClientRepository;
import com.basilios.basilios.infra.repository.AddressRepository;
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
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {


        // 2. Normalizar CPF (remover formatação)
        String cpfNormalizado = normalizarCpf(request.getCpf());

        // 3. Validar unicidade
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }


        if (usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new BusinessException("CPF já cadastrado");
        }

        // 5. Criar Cliente (novo usuário sempre é Cliente)
        Client client = Client.builder()
                .nomeUsuario(request.getNomeUsuario())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(cpfNormalizado)
                .telefone(normalizarTelefone(request.getTelefone()))
                .roles(List.of(RoleEnum.ROLE_CLIENTE))
                .enabled(true)
                .build();



        // 7. Salvar cliente
        client = clientRepository.save(client);

        // 8. Gerar token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(client.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // 9. Retornar resposta
        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(client.getId())
                .name(client.getNomeUsuario())
                .email(client.getEmail())
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            // Autenticar usando apenas email
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Credenciais inválidas");
        }

        // Buscar usuário por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Usuário não encontrado"));

        // Verificar se está ativo
        if (!usuario.isAtivo()) {
            throw new AuthenticationException("Usuário desativado");
        }

        // Gerar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(usuario.getId())
                .name(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .build();
    }

    /**
     * Normaliza CPF removendo pontos e traços
     * Aceita: 123.456.789-01 ou 12345678901
     * Retorna: 12345678901
     */
    private String normalizarCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    /**
     * Normaliza telefone removendo caracteres não numéricos
     * Aceita: (11) 99999-9999 ou 11999999999
     * Retorna: 11999999999
     */
    private String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        return telefone.replaceAll("[^0-9]", "");
    }
}