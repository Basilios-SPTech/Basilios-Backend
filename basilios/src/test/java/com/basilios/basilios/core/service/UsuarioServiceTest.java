package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService Tests")
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
    private Address address;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeUsuario("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setCpf("12345678900");
        usuario.setTelefone("11987654321");
        usuario.setRoles(new ArrayList<>(Arrays.asList(RoleEnum.ROLE_CLIENTE)));
        usuario.setAddresses(new ArrayList<>());
        usuario.setEnabled(true);
        usuario.setDeletedAt(null);

        address = new Address();
        address.setIdAddress(1L);
        address.setRua("Av. Paulista");
        address.setNumero("1000");
        address.setCidade("São Paulo");
        address.setEstado("SP");
        address.setUsuario(usuario);
    }

    @Nested
    @DisplayName("getCurrentUsuario Tests")
    class GetCurrentUsuarioTests {

        @Test
        @DisplayName("Deve retornar usuário autenticado do contexto de segurança")
        void shouldReturnAuthenticatedUserFromSecurityContext() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("joao@email.com");
            SecurityContextHolder.setContext(securityContext);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            Usuario result = usuarioService.getCurrentUsuario();

            assertNotNull(result);
            assertEquals("joao@email.com", result.getEmail());
            verify(usuarioRepository).findByEmail("joao@email.com");
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não encontrado")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("inexistente@email.com");
            SecurityContextHolder.setContext(securityContext);
            when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.getCurrentUsuario()
            );

            assertEquals("Usuário não encontrado", exception.getMessage());
        }

        @Test
        @DisplayName("Deve buscar usuário pelo email do authentication")
        void shouldSearchUserByAuthenticationEmail() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("joao@email.com");
            SecurityContextHolder.setContext(securityContext);
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            usuarioService.getCurrentUsuario();

            verify(authentication).getName();
            verify(usuarioRepository).findByEmail("joao@email.com");
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar usuário quando ID existe")
        void shouldReturnUserWhenIdExists() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            Usuario result = usuarioService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("João Silva", result.getNomeUsuario());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando ID não existe")
        void shouldThrowNotFoundExceptionWhenIdNotExists() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.findById(999L)
            );

            assertTrue(exception.getMessage().contains("Usuário não encontrado: 999"));
        }

        @Test
        @DisplayName("Deve buscar usuário pelo ID correto")
        void shouldSearchUserByCorrectId() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            usuarioService.findById(1L);

            verify(usuarioRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("findByEmail Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Deve retornar usuário quando email existe")
        void shouldReturnUserWhenEmailExists() {
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            Usuario result = usuarioService.findByEmail("joao@email.com");

            assertNotNull(result);
            assertEquals("joao@email.com", result.getEmail());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando email não existe")
        void shouldThrowNotFoundExceptionWhenEmailNotExists() {
            when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.findByEmail("inexistente@email.com")
            );

            assertTrue(exception.getMessage().contains("Usuário não encontrado: inexistente@email.com"));
        }

        @Test
        @DisplayName("Deve buscar usuário pelo email correto")
        void shouldSearchUserByCorrectEmail() {
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            usuarioService.findByEmail("joao@email.com");

            verify(usuarioRepository).findByEmail("joao@email.com");
        }
    }

    @Nested
    @DisplayName("findByNomeUsuario Tests")
    class FindByNomeUsuarioTests {

        @Test
        @DisplayName("Deve retornar usuário quando nomeUsuario existe")
        void shouldReturnUserWhenNomeUsuarioExists() {
            when(usuarioRepository.findByNomeUsuario("João Silva")).thenReturn(Optional.of(usuario));

            Usuario result = usuarioService.findByNomeUsuario("João Silva");

            assertNotNull(result);
            assertEquals("João Silva", result.getNomeUsuario());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando nomeUsuario não existe")
        void shouldThrowNotFoundExceptionWhenNomeUsuarioNotExists() {
            when(usuarioRepository.findByNomeUsuario("Inexistente")).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.findByNomeUsuario("Inexistente")
            );

            assertTrue(exception.getMessage().contains("Usuário não encontrado: Inexistente"));
        }

        @Test
        @DisplayName("Deve buscar usuário pelo nomeUsuario correto")
        void shouldSearchUserByCorrectNomeUsuario() {
            when(usuarioRepository.findByNomeUsuario("João Silva")).thenReturn(Optional.of(usuario));

            usuarioService.findByNomeUsuario("João Silva");

            verify(usuarioRepository).findByNomeUsuario("João Silva");
        }
    }

    @Nested
    @DisplayName("findByCpf Tests")
    class FindByCpfTests {

        @Test
        @DisplayName("Deve retornar usuário quando CPF existe")
        void shouldReturnUserWhenCpfExists() {
            when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

            Usuario result = usuarioService.findByCpf("12345678900");

            assertNotNull(result);
            assertEquals("12345678900", result.getCpf());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando CPF não existe")
        void shouldThrowNotFoundExceptionWhenCpfNotExists() {
            when(usuarioRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.findByCpf("99999999999")
            );

            assertTrue(exception.getMessage().contains("Usuário não encontrado com CPF: 99999999999"));
        }

        @Test
        @DisplayName("Deve buscar usuário pelo CPF correto")
        void shouldSearchUserByCorrectCpf() {
            when(usuarioRepository.findByCpf("12345678900")).thenReturn(Optional.of(usuario));

            usuarioService.findByCpf("12345678900");

            verify(usuarioRepository).findByCpf("12345678900");
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Deve retornar lista de todos os usuários")
        void shouldReturnListOfAllUsers() {
            Usuario usuario2 = new Usuario();
            usuario2.setId(2L);
            usuario2.setNomeUsuario("Maria Santos");

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            List<Usuario> result = usuarioService.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários")
        void shouldReturnEmptyListWhenNoUsers() {
            when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

            List<Usuario> result = usuarioService.findAll();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve chamar repositório para buscar todos usuários")
        void shouldCallRepositoryToFindAllUsers() {
            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));

            usuarioService.findAll();

            verify(usuarioRepository).findAll();
        }
    }

    @Nested
    @DisplayName("addRole Tests")
    class AddRoleTests {

        @Test
        @DisplayName("Deve adicionar role ao usuário com sucesso")
        void shouldAddRoleToUserSuccessfully() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.addRole(1L, RoleEnum.ROLE_FUNCIONARIO);

            assertNotNull(result);
            assertTrue(result.getRoles().contains(RoleEnum.ROLE_FUNCIONARIO));
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário já possui a role")
        void shouldThrowBusinessExceptionWhenUserAlreadyHasRole() {
            usuario.getRoles().add(RoleEnum.ROLE_FUNCIONARIO);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.addRole(1L, RoleEnum.ROLE_FUNCIONARIO)
            );

            assertTrue(exception.getMessage().contains("Usuário já possui a role"));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotExists() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    usuarioService.addRole(999L, RoleEnum.ROLE_FUNCIONARIO)
            );
        }
    }

    @Nested
    @DisplayName("removeRole Tests")
    class RemoveRoleTests {

        @Test
        @DisplayName("Deve remover role do usuário com sucesso")
        void shouldRemoveRoleFromUserSuccessfully() {
            usuario.getRoles().add(RoleEnum.ROLE_FUNCIONARIO);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.removeRole(1L, RoleEnum.ROLE_FUNCIONARIO);

            assertNotNull(result);
            assertFalse(result.getRoles().contains(RoleEnum.ROLE_FUNCIONARIO));
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário não possui a role")
        void shouldThrowBusinessExceptionWhenUserDoesNotHaveRole() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.removeRole(1L, RoleEnum.ROLE_FUNCIONARIO)
            );

            assertTrue(exception.getMessage().contains("Usuário não possui a role"));
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao remover ROLE_CLIENTE se for única role")
        void shouldThrowBusinessExceptionWhenRemovingClienteAsOnlyRole() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.removeRole(1L, RoleEnum.ROLE_CLIENTE)
            );

            assertTrue(exception.getMessage().contains("Não é possível remover ROLE_CLIENTE"));
        }
    }

    @Nested
    @DisplayName("addAddress Tests")
    class AddAddressTests {

        @Test
        @DisplayName("Deve adicionar endereço ao usuário com sucesso")
        void shouldAddAddressToUserSuccessfully() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.addAddress(1L, address);

            assertNotNull(result);
            assertTrue(result.getAddresses().contains(address));
            assertEquals(usuario, address.getUsuario());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve definir primeiro endereço como principal")
        void shouldSetFirstAddressAsPrincipal() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.addAddress(1L, address);

            assertEquals(address, result.getAddressPrincipal());
        }

        @Test
        @DisplayName("Não deve alterar endereço principal se já existir um")
        void shouldNotChangePrincipalAddressIfOneExists() {
            Address addressPrincipal = new Address();
            addressPrincipal.setIdAddress(2L);
            usuario.setAddressPrincipal(addressPrincipal);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.addAddress(1L, address);

            assertEquals(addressPrincipal, result.getAddressPrincipal());
        }
    }

    @Nested
    @DisplayName("removeAddress Tests")
    class RemoveAddressTests {

        @Test
        @DisplayName("Deve remover endereço do usuário com sucesso")
        void shouldRemoveAddressFromUserSuccessfully() {
            usuario.getAddresses().add(address);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.removeAddress(1L, address);

            assertNotNull(result);
            assertFalse(result.getAddresses().contains(address));
            assertNull(address.getUsuario());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve definir endereço principal como null ao remover último endereço")
        void shouldSetPrincipalAddressAsNullWhenRemovingLastAddress() {
            usuario.getAddresses().add(address);
            usuario.setAddressPrincipal(address);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.removeAddress(1L, address);

            assertNull(result.getAddressPrincipal());
        }
    }

    @Nested
    @DisplayName("setEnderecoPrincipal Tests")
    class SetEnderecoPrincipalTests {

        @Test
        @DisplayName("Deve definir endereço principal com sucesso")
        void shouldSetPrincipalAddressSuccessfully() {
            usuario.getAddresses().add(address);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.setEnderecoPrincipal(1L, 1L);

            assertNotNull(result);
            assertEquals(address, result.getAddressPrincipal());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando endereço não pertence ao usuário")
        void shouldThrowNotFoundExceptionWhenAddressDoesNotBelongToUser() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    usuarioService.setEnderecoPrincipal(1L, 999L)
            );

            assertTrue(exception.getMessage().contains("Endereço não encontrado para este usuário"));
        }

        @Test
        @DisplayName("Deve alterar endereço principal existente")
        void shouldChangePrincipalAddress() {
            Address address2 = new Address();
            address2.setIdAddress(2L);
            usuario.getAddresses().add(address);
            usuario.getAddresses().add(address2);
            usuario.setAddressPrincipal(address);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.setEnderecoPrincipal(1L, 2L);

            assertEquals(address2, result.getAddressPrincipal());
        }
    }

    @Nested
    @DisplayName("updateUsuario Tests")
    class UpdateUsuarioTests {

        @Test
        @DisplayName("Deve atualizar dados básicos do usuário com sucesso")
        void shouldUpdateBasicUserDataSuccessfully() {
            Usuario dadosAtualizados = new Usuario();
            dadosAtualizados.setNomeUsuario("João Silva Atualizado");
            dadosAtualizados.setEmail("joao@email.com");
            dadosAtualizados.setTelefone("11999887766");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.updateUsuario(1L, dadosAtualizados);

            assertNotNull(result);
            assertEquals("João Silva Atualizado", result.getNomeUsuario());
            assertEquals("11999887766", result.getTelefone());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já existe")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            Usuario dadosAtualizados = new Usuario();
            dadosAtualizados.setNomeUsuario("João Silva");
            dadosAtualizados.setEmail("outro@email.com");
            dadosAtualizados.setTelefone("11987654321");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.updateUsuario(1L, dadosAtualizados)
            );

            assertEquals("Email já cadastrado", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando nomeUsuario já existe")
        void shouldThrowBusinessExceptionWhenNomeUsuarioAlreadyExists() {
            Usuario dadosAtualizados = new Usuario();
            dadosAtualizados.setNomeUsuario("Maria Santos");
            dadosAtualizados.setEmail("joao@email.com");
            dadosAtualizados.setTelefone("11987654321");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByNomeUsuario("Maria Santos")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.updateUsuario(1L, dadosAtualizados)
            );

            assertEquals("Nome de usuário já existe", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("desativarUsuario Tests")
    class DesativarUsuarioTests {

        @Test
        @DisplayName("Deve desativar usuário com sucesso")
        void shouldDeactivateUserSuccessfully() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            usuarioService.desativarUsuario(1L);

            assertNotNull(usuario.getDeletedAt());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotExists() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    usuarioService.desativarUsuario(999L)
            );
        }

        @Test
        @DisplayName("Deve fazer soft delete marcando deletedAt")
        void shouldDoSoftDeleteByMarkingDeletedAt() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            usuarioService.desativarUsuario(1L);

            assertNotNull(usuario.getDeletedAt());
        }
    }

    @Nested
    @DisplayName("reativarUsuario Tests")
    class ReativarUsuarioTests {

        @Test
        @DisplayName("Deve reativar usuário desativado com sucesso")
        void shouldReactivateDeactivatedUserSuccessfully() {
            usuario.setDeletedAt(LocalDateTime.now());
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            Usuario result = usuarioService.reativarUsuario(1L);

            assertNotNull(result);
            assertNull(result.getDeletedAt());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário já está ativo")
        void shouldThrowBusinessExceptionWhenUserAlreadyActive() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    usuarioService.reativarUsuario(1L)
            );

            assertEquals("Usuário já está ativo", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotExists() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    usuarioService.reativarUsuario(999L)
            );
        }
    }

    @Nested
    @DisplayName("countActiveUsuarios Tests")
    class CountActiveUsuariosTests {

        @Test
        @DisplayName("Deve contar apenas usuários ativos")
        void shouldCountOnlyActiveUsers() {
            Usuario usuario2 = new Usuario();
            usuario2.setId(2L);
            usuario2.setDeletedAt(LocalDateTime.now());

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            long result = usuarioService.countActiveUsuarios();

            assertEquals(1L, result);
        }

        @Test
        @DisplayName("Deve retornar zero quando não há usuários ativos")
        void shouldReturnZeroWhenNoActiveUsers() {
            usuario.setDeletedAt(LocalDateTime.now());
            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));

            long result = usuarioService.countActiveUsuarios();

            assertEquals(0L, result);
        }

        @Test
        @DisplayName("Deve contar todos usuários quando todos estão ativos")
        void shouldCountAllUsersWhenAllActive() {
            Usuario usuario2 = new Usuario();
            usuario2.setId(2L);
            usuario2.setDeletedAt(null);

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            long result = usuarioService.countActiveUsuarios();

            assertEquals(2L, result);
        }
    }

    @Nested
    @DisplayName("findByRole Tests")
    class FindByRoleTests {

        @Test
        @DisplayName("Deve retornar usuários com a role específica")
        void shouldReturnUsersWithSpecificRole() {
            Usuario usuario2 = new Usuario();
            usuario2.setId(2L);
            usuario2.setRoles(new ArrayList<>(Arrays.asList(RoleEnum.ROLE_FUNCIONARIO)));

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            List<Usuario> result = usuarioService.findByRole(RoleEnum.ROLE_CLIENTE);

            assertEquals(1, result.size());
            assertTrue(result.get(0).hasRole(RoleEnum.ROLE_CLIENTE));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum usuário tem a role")
        void shouldReturnEmptyListWhenNoUserHasRole() {
            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario));

            List<Usuario> result = usuarioService.findByRole(RoleEnum.ROLE_FUNCIONARIO);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve filtrar corretamente usuários por múltiplas roles")
        void shouldCorrectlyFilterUsersByMultipleRoles() {
            usuario.getRoles().add(RoleEnum.ROLE_FUNCIONARIO);
            Usuario usuario2 = new Usuario();
            usuario2.setId(2L);
            usuario2.setRoles(new ArrayList<>(Arrays.asList(RoleEnum.ROLE_CLIENTE)));

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            List<Usuario> result = usuarioService.findByRole(RoleEnum.ROLE_FUNCIONARIO);

            assertEquals(1, result.size());
            assertTrue(result.get(0).hasRole(RoleEnum.ROLE_FUNCIONARIO));
        }
    }

    @Nested
    @DisplayName("isFuncionario Tests")
    class IsFuncionarioTests {

        @Test
        @DisplayName("Deve retornar true quando usuário é funcionário")
        void shouldReturnTrueWhenUserIsFuncionario() {
            usuario.getRoles().add(RoleEnum.ROLE_FUNCIONARIO);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            boolean result = usuarioService.isFuncionario(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não é funcionário")
        void shouldReturnFalseWhenUserIsNotFuncionario() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            boolean result = usuarioService.isFuncionario(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotExists() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    usuarioService.isFuncionario(999L)
            );
        }
    }
}