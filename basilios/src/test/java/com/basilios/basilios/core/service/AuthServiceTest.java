package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.AuthenticationException;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import com.basilios.basilios.infra.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("teste@teste.com");
        usuario.setEnabled(true);
        usuario.setNomeUsuario("Mouras");
        usuario.setRoles(List.of(RoleEnum.ROLE_CLIENTE));

        userDetails = mock(UserDetails.class);
    }

    // ============================================================
    //  register() → POSITIVO
    // ============================================================

    @Test
    @DisplayName("Deve registrar um usuário quando os dados forem válidos")
    void register_DeveRegistrarUsuarioQuandoDadosValidos() {
        UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
        dto.setEmail("novo@teste.com");
        dto.setPassword("123");
        dto.setCpf("123.456.789-00");
        dto.setTelefone("(11) 90000-0000");
        dto.setNomeUsuario("Novo Usuário");

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        UsuarioTokenDTO result = authService.register(dto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Mouras", result.getNomeUsuario());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ============================================================
    //  register() → NEGATIVO (email duplicado)
    // ============================================================

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar registrar com email duplicado")
    void register_DeveLancarException_QuandoEmailDuplicado() {
        UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
        dto.setEmail("existente@teste.com");
        dto.setCpf("123");

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(dto));

        verify(usuarioRepository, times(1)).existsByEmail(dto.getEmail());
        verify(usuarioRepository, never()).save(any());
    }

    // ============================================================
    //  login() → POSITIVO
    // ============================================================

    @Test
    @DisplayName("Deve retornar token JWT quando as credenciais forem válidas no login")
    void login_DeveRetornarTokenQuandoCredenciaisValidas() {
        UsuarioLoginDTO dto = new UsuarioLoginDTO();
        dto.setEmail("teste@teste.com");
        dto.setPassword("123");

        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(usuario));
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");

        UsuarioTokenDTO result = authService.login(dto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Mouras", result.getNomeUsuario());
        verify(authenticationManager).authenticate(any());
    }

    // ============================================================
    //  login() → NEGATIVO (senha incorreta)
    // ============================================================

    @Test
    @DisplayName("Deve lançar AuthenticationException quando a senha estiver incorreta no login")
    void login_DeveLancarAuthenticationException_QuandoSenhaIncorreta() {
        UsuarioLoginDTO dto = new UsuarioLoginDTO();
        dto.setEmail("teste@teste.com");
        dto.setPassword("senhaErrada");

        doThrow(new BadCredentialsException("Credenciais inválidas"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(AuthenticationException.class, () -> authService.login(dto));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(usuarioRepository, never()).findByEmail(any());
    }
}
