package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.auth.AuthResponse;
import com.basilios.basilios.app.dto.auth.LoginRequest;
import com.basilios.basilios.app.dto.auth.RegisterRequest;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.AuthenticationException;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.model.Cliente;
import com.basilios.basilios.core.model.Endereco;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.ClienteRepository;
import com.basilios.basilios.infra.repository.EnderecoRepository;
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

import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Validar senhas coincidem
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("As senhas não coincidem");
        }

        // 2. Normalizar CPF (remover formatação)
        String cpfNormalizado = normalizarCpf(request.getCpf());

        // 3. Validar unicidade
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (usuarioRepository.existsByNomeUsuario(request.getNomeUsuario())) {
            throw new BusinessException("Nome de usuário já existe");
        }

        if (usuarioRepository.existsByCpf(cpfNormalizado)) {
            throw new BusinessException("CPF já cadastrado");
        }

        // 4. Criar e salvar endereço
        Endereco endereco = Endereco.builder()
                .rua(request.getRua())
                .numero(request.getNumero())
                .bairro(request.getBairro())
                .complemento(request.getComplemento())
                .cep(request.getCep())
                .cidade(request.getCidade() != null ? request.getCidade() : "São Paulo")
                .estado(request.getEstado() != null ? request.getEstado() : "SP")
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        // 5. Criar Cliente (novo usuário sempre é Cliente)
        Cliente cliente = Cliente.builder()
                .nomeUsuario(request.getNomeUsuario())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cpf(cpfNormalizado)
                .telefone(normalizarTelefone(request.getTelefone()))
                .dataNascimento(request.getDataNascimento())
                .roles(Set.of(RoleEnum.ROLE_CLIENTE))
                .enabled(true)
                .build();

        // 6. Associar endereço ao cliente
        endereco.setUsuario(cliente);
        endereco = enderecoRepository.save(endereco);

        cliente.addEndereco(endereco);
        cliente.setEnderecoPrincipal(endereco);

        // 7. Salvar cliente
        cliente = clienteRepository.save(cliente);

        // 8. Gerar token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(cliente.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // 9. Retornar resposta
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(cliente.getId())
                .name(cliente.getNomeUsuario())
                .email(cliente.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar usando email ou nomeUsuario
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new AuthenticationException("Credenciais inválidas");
        }

        // Buscar usuário por email ou nomeUsuario
        Usuario usuario = usuarioRepository.findByEmailOrNomeUsuario(request.getEmail(), request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Usuário não encontrado"));

        // Verificar se está ativo
        if (!usuario.isAtivo()) {
            throw new AuthenticationException("Usuário desativado");
        }

        // Gerar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
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