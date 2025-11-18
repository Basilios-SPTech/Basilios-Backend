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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
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

    private UsuarioRegisterDTO registerDTO;
    private UsuarioLoginDTO loginDTO;
    private Usuario usuario;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerDTO = new UsuarioRegisterDTO();
        registerDTO.setNomeUsuario("João Silva");
        registerDTO.setEmail("joao@email.com");
        registerDTO.setPassword("senha123");
        registerDTO.setCpf("123.456.789-00");
        registerDTO.setTelefone("(11) 98765-4321");

        loginDTO = new UsuarioLoginDTO();
        loginDTO.setEmail("joao@email.com");
        loginDTO.setPassword("senha123");

        usuario = Usuario.builder()
                .id(1L)
                .nomeUsuario("João Silva")
                .email("joao@email.com")
                .password("$2a$10$encodedPassword")
                .cpf("12345678900")
                .telefone("11987654321")
                .roles(List.of(RoleEnum.ROLE_CLIENTE))
                .enabled(true)
                .build();

        userDetails = User.builder()
                .username("joao@email.com")
                .password("$2a$10$encodedPassword")
                .authorities(Collections.emptyList())
                .build();
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Deve registrar novo usuário com sucesso")
        void shouldRegisterNewUserSuccessfully() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.register(registerDTO);

            assertNotNull(result);
            assertEquals("jwt-token-123", result.getToken());
            assertEquals(1L, result.getId());
            assertEquals("João Silva", result.getNomeUsuario());
            assertEquals("joao@email.com", result.getEmail());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já existe")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    authService.register(registerDTO)
            );

            assertEquals("Email já cadastrado", exception.getMessage());
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando CPF já existe")
        void shouldThrowBusinessExceptionWhenCpfAlreadyExists() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf("12345678900")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    authService.register(registerDTO)
            );

            assertEquals("CPF já cadastrado", exception.getMessage());
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve normalizar CPF removendo caracteres especiais")
        void shouldNormalizeCpfRemovingSpecialCharacters() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario savedUser = invocation.getArgument(0);
                assertEquals("12345678900", savedUser.getCpf());
                return usuario;
            });
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            authService.register(registerDTO);

            verify(usuarioRepository).existsByCpf("12345678900");
        }

        @Test
        @DisplayName("Deve normalizar telefone removendo caracteres especiais")
        void shouldNormalizeTelefoneRemovingSpecialCharacters() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario savedUser = invocation.getArgument(0);
                assertEquals("11987654321", savedUser.getTelefone());
                return usuario;
            });
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            authService.register(registerDTO);

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve criar usuário com role CLIENTE por padrão")
        void shouldCreateUserWithClienteRoleByDefault() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario savedUser = invocation.getArgument(0);
                assertTrue(savedUser.getRoles().contains(RoleEnum.ROLE_CLIENTE));
                assertEquals(1, savedUser.getRoles().size());
                return usuario;
            });
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            authService.register(registerDTO);

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve criar usuário habilitado por padrão")
        void shouldCreateEnabledUserByDefault() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario savedUser = invocation.getArgument(0);
                assertTrue(savedUser.getEnabled());
                return usuario;
            });
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            authService.register(registerDTO);

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve criptografar senha antes de salvar")
        void shouldEncryptPasswordBeforeSaving() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token-123");

            authService.register(registerDTO);

            verify(passwordEncoder).encode("senha123");
        }

        @Test
        @DisplayName("Deve gerar token JWT após registro bem-sucedido")
        void shouldGenerateJwtTokenAfterSuccessfulRegistration() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.register(registerDTO);

            assertNotNull(result.getToken());
            verify(jwtUtil).generateToken(userDetails);
        }
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("Deve fazer login com sucesso com credenciais válidas")
        void shouldLoginSuccessfullyWithValidCredentials() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.login(loginDTO);

            assertNotNull(result);
            assertEquals("jwt-token-123", result.getToken());
            assertEquals(1L, result.getId());
            assertEquals("João Silva", result.getNomeUsuario());
            assertEquals("joao@email.com", result.getEmail());
        }

        @Test
        @DisplayName("Deve lançar AuthenticationException com credenciais inválidas")
        void shouldThrowAuthenticationExceptionWithInvalidCredentials() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                    authService.login(loginDTO)
            );

            assertEquals("Credenciais inválidas", exception.getMessage());
            verify(usuarioRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Deve lançar AuthenticationException quando usuário não encontrado")
        void shouldThrowAuthenticationExceptionWhenUserNotFound() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

            AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                    authService.login(loginDTO)
            );

            assertEquals("Usuário não encontrado", exception.getMessage());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve lançar AuthenticationException quando usuário está desativado")
        void shouldThrowAuthenticationExceptionWhenUserIsDisabled() {
            usuario.setEnabled(false);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                    authService.login(loginDTO)
            );

            assertEquals("Usuário desativado", exception.getMessage());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve gerar token JWT após login bem-sucedido")
        void shouldGenerateJwtTokenAfterSuccessfulLogin() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.login(loginDTO);

            assertNotNull(result.getToken());
            verify(jwtUtil).generateToken(userDetails);
        }

        @Test
        @DisplayName("Deve autenticar usando AuthenticationManager")
        void shouldAuthenticateUsingAuthenticationManager() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            authService.login(loginDTO);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Deve carregar UserDetails pelo email do usuário")
        void shouldLoadUserDetailsByEmail() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            authService.login(loginDTO);

            verify(userDetailsService).loadUserByUsername("joao@email.com");
        }

        @Test
        @DisplayName("Deve retornar DTO com informações completas do usuário")
        void shouldReturnDtoWithCompleteUserInformation() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.login(loginDTO);

            assertAll("UsuarioTokenDTO",
                    () -> assertNotNull(result.getToken()),
                    () -> assertNotNull(result.getId()),
                    () -> assertNotNull(result.getNomeUsuario()),
                    () -> assertNotNull(result.getEmail())
            );
        }

        @Test
        @DisplayName("Deve permitir login apenas para usuários habilitados")
        void shouldAllowLoginOnlyForEnabledUsers() {
            usuario.setEnabled(true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

            UsuarioTokenDTO result = authService.login(loginDTO);

            assertNotNull(result);
            assertTrue(usuario.getEnabled());
        }
    }

    @Nested
    @DisplayName("Normalização Tests")
    class NormalizacaoTests {

        @Test
        @DisplayName("Deve normalizar CPF com pontos e hífen")
        void shouldNormalizeCpfWithDotsAndDash() {
            UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
            dto.setNomeUsuario("Test");
            dto.setEmail("test@email.com");
            dto.setPassword("senha123");
            dto.setCpf("123.456.789-00");
            dto.setTelefone("11987654321");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf("12345678900")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");

            authService.register(dto);

            verify(usuarioRepository).existsByCpf("12345678900");
        }

        @Test
        @DisplayName("Deve normalizar telefone com parênteses e hífen")
        void shouldNormalizeTelefoneWithParenthesesAndDash() {
            UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
            dto.setNomeUsuario("Test");
            dto.setEmail("test@email.com");
            dto.setPassword("senha123");
            dto.setCpf("12345678900");
            dto.setTelefone("(11) 98765-4321");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario saved = invocation.getArgument(0);
                assertEquals("11987654321", saved.getTelefone());
                return usuario;
            });
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");

            authService.register(dto);

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lidar com CPF nulo")
        void shouldHandleNullCpf() {
            UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
            dto.setNomeUsuario("Test");
            dto.setEmail("test@email.com");
            dto.setPassword("senha123");
            dto.setCpf(null);
            dto.setTelefone("11987654321");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(null)).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");

            assertDoesNotThrow(() -> authService.register(dto));
        }
    }

    @Nested
    @DisplayName("Integração Token Tests")
    class IntegracaoTokenTests {

        @Test
        @DisplayName("Deve gerar token válido no registro e retornar no DTO")
        void shouldGenerateValidTokenOnRegisterAndReturnInDto() {
            String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid-token";

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

            UsuarioTokenDTO result = authService.register(registerDTO);

            assertEquals(expectedToken, result.getToken());
        }

        @Test
        @DisplayName("Deve gerar token válido no login e retornar no DTO")
        void shouldGenerateValidTokenOnLoginAndReturnInDto() {
            String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid-token";

            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
            when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

            UsuarioTokenDTO result = authService.login(loginDTO);

            assertEquals(expectedToken, result.getToken());
        }

        @Test
        @DisplayName("Deve usar UserDetails para gerar token")
        void shouldUseUserDetailsToGenerateToken() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("token");

            authService.register(registerDTO);

            verify(userDetailsService).loadUserByUsername("joao@email.com");
            verify(jwtUtil).generateToken(userDetails);
        }
    }
}