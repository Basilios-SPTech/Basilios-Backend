package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Criar usuário mock
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeUsuario("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setCpf("12345678900");
        usuario.setTelefone("11987654321");
        usuario.setDataNascimento(LocalDate.of(1990, 5, 15));
        usuario.setEnabled(true);

        List<RoleEnum> roles = new ArrayList<>();
        roles.add(RoleEnum.ROLE_CLIENTE);
        usuario.setRoles(roles);
    }

    @AfterEach
    void tearDown() {
        // Limpar o SecurityContext após cada teste
        SecurityContextHolder.clearContext();
    }

    // ========== TESTES DO MÉTODO getCurrentUsuario() ==========

    @Test
    @DisplayName("Deve retornar usuário autenticado com sucesso")
    void getCurrentUsuario_DeveRetornarUsuarioAutenticado() {
        // Arrange
        String email = "joao@email.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        SecurityContextHolder.setContext(securityContext);

        // Act
        Usuario result = usuarioService.getCurrentUsuario();

        // Assert
        assertNotNull(result);
        assertEquals(usuario.getId(), result.getId());
        assertEquals(email, result.getEmail());
        assertEquals("João Silva", result.getNomeUsuario());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando usuário não existe no banco")
    void getCurrentUsuario_DeveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        String email = "naoexiste@email.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usuarioService.getCurrentUsuario());

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve buscar usuário pelo email do authentication context")
    void getCurrentUsuario_DeveBuscarPorEmailDoContext() {
        // Arrange
        String email = "teste@email.com";
        usuario.setEmail(email);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        SecurityContextHolder.setContext(securityContext);

        // Act
        Usuario result = usuarioService.getCurrentUsuario();

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(authentication, times(1)).getName();
        verify(usuarioRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar usuário com roles corretas do contexto de segurança")
    void getCurrentUsuario_DeveRetornarUsuarioComRolesCorretas() {
        // Arrange
        String email = "admin@email.com";
        usuario.setEmail(email);
        usuario.getRoles().add(RoleEnum.ROLE_ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        SecurityContextHolder.setContext(securityContext);

        // Act
        Usuario result = usuarioService.getCurrentUsuario();

        // Assert
        assertNotNull(result);
        assertTrue(result.hasRole(RoleEnum.ROLE_CLIENTE));
        assertTrue(result.hasRole(RoleEnum.ROLE_ADMIN));
        assertEquals(2, result.getRoles().size());
    }

    // ========== TESTES DO MÉTODO addRole() ==========

    @Test
    @DisplayName("Deve adicionar role ao usuário com sucesso")
    void addRole_DeveAdicionarRoleComSucesso() {
        // Arrange
        RoleEnum novaRole = RoleEnum.ROLE_FUNCIONARIO;

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario result = usuarioService.addRole(1L, novaRole);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasRole(RoleEnum.ROLE_CLIENTE));
        assertTrue(result.hasRole(RoleEnum.ROLE_FUNCIONARIO));
        assertEquals(2, result.getRoles().size());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando usuário já possui a role")
    void addRole_DeveLancarExcecaoQuandoUsuarioJaPossuiRole() {
        // Arrange
        RoleEnum roleExistente = RoleEnum.ROLE_CLIENTE;

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> usuarioService.addRole(1L, roleExistente));

        assertEquals("Usuário já possui a role: ROLE_CLIENTE", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando usuário não existe")
    void addRole_DeveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Arrange
        Long usuarioIdInexistente = 999L;
        RoleEnum role = RoleEnum.ROLE_ADMIN;

        when(usuarioRepository.findById(usuarioIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usuarioService.addRole(usuarioIdInexistente, role));

        assertEquals("Usuário não encontrado: 999", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(usuarioIdInexistente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve adicionar role ADMIN a usuário que já é CLIENTE")
    void addRole_DeveAdicionarRoleAdminAUsuarioCliente() {
        // Arrange
        RoleEnum roleAdmin = RoleEnum.ROLE_ADMIN;

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario result = usuarioService.addRole(1L, roleAdmin);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasRole(RoleEnum.ROLE_CLIENTE));
        assertTrue(result.hasRole(RoleEnum.ROLE_ADMIN));
        assertEquals(2, result.getRoles().size());
    }

    @Test
    @DisplayName("Deve adicionar múltiplas roles sequencialmente")
    void addRole_DeveAdicionarMultiplasRolesSequencialmente() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.addRole(1L, RoleEnum.ROLE_FUNCIONARIO);
        usuarioService.addRole(1L, RoleEnum.ROLE_ADMIN);

        // Assert
        assertTrue(usuario.hasRole(RoleEnum.ROLE_CLIENTE));
        assertTrue(usuario.hasRole(RoleEnum.ROLE_FUNCIONARIO));
        assertTrue(usuario.hasRole(RoleEnum.ROLE_ADMIN));
        assertEquals(3, usuario.getRoles().size());
        verify(usuarioRepository, times(2)).findById(1L);
        verify(usuarioRepository, times(2)).save(usuario);
    }
}