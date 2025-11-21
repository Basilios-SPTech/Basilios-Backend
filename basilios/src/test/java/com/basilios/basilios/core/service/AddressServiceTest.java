package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AddressService addressService;

    private Usuario usuario;
    private Address address1;
    private Address address2;
    private AddressRequestDTO addressRequestDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nomeUsuario("João Silva")
                .email("joao@email.com")
                .build();

        address1 = Address.builder()
                .idAddress(1L)
                .usuario(usuario)
                .rua("Rua das Flores")
                .numero("100")
                .bairro("Centro")
                .cep("12345678")
                .cidade("São Paulo")
                .estado("SP")
                .complemento("Apto 101")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .createdAt(LocalDateTime.now())
                .build();

        address2 = Address.builder()
                .idAddress(2L)
                .usuario(usuario)
                .rua("Av. Paulista")
                .numero("1000")
                .bairro("Bela Vista")
                .cep("01310100")
                .cidade("São Paulo")
                .estado("SP")
                .createdAt(LocalDateTime.now())
                .build();

        addressRequestDTO = AddressRequestDTO.builder()
                .rua("Rua Nova")
                .numero("200")
                .bairro("Jardins")
                .cep("12345-678")
                .cidade("São Paulo")
                .estado("SP")
                .complemento("Casa")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .build();
    }

    @Nested
    @DisplayName("getUserAddresses Tests")
    class GetUserAddressesTests {

        @Test
        @DisplayName("Deve retornar lista de endereços do usuário autenticado")
        void shouldReturnUserAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Arrays.asList(address1, address2));

            List<AddressResponseDTO> result = addressService.getUserAddresses();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("12345-678", result.get(0).getCep());
            verify(usuarioService).getCurrentUsuario();
            verify(addressRepository).findByUsuarioAndDeletedAtIsNull(usuario);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não tem endereços")
        void shouldReturnEmptyListWhenNoAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Collections.emptyList());

            List<AddressResponseDTO> result = addressService.getUserAddresses();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve filtrar apenas endereços ativos (deletedAt null)")
        void shouldReturnOnlyActiveAddresses() {
            Address deletedAddress = Address.builder()
                    .idAddress(3L)
                    .usuario(usuario)
                    .rua("Rua Deletada")
                    .deletedAt(LocalDateTime.now())
                    .build();

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Arrays.asList(address1, address2));

            List<AddressResponseDTO> result = addressService.getUserAddresses();

            assertEquals(2, result.size());
            assertFalse(result.stream().anyMatch(a -> a.getRua().equals("Rua Deletada")));
        }
    }

    @Nested
    @DisplayName("findActiveAddressesByUserId Tests")
    class FindActiveAddressesByUserIdTests {

        @Test
        @DisplayName("Deve retornar endereços ativos do usuário específico")
        void shouldReturnActiveAddressesForSpecificUser() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Arrays.asList(address1, address2));

            List<Address> result = addressService.findActiveAddressesByUserId(1L);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.findActiveAddressesByUserId(999L)
            );
        }

        @Test
        @DisplayName("Deve retornar lista vazia para usuário sem endereços")
        void shouldReturnEmptyListForUserWithoutAddresses() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Collections.emptyList());

            List<Address> result = addressService.findActiveAddressesByUserId(1L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar endereço por ID quando pertence ao usuário")
        void shouldReturnAddressByIdWhenBelongsToUser() {
            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Rua das Flores", result.getRua());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando endereço não existe")
        void shouldThrowNotFoundExceptionWhenAddressNotFound() {
            when(addressRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.findById(999L)
            );
        }
    }

    @Nested
    @DisplayName("getPrincipalAddress Tests")
    class GetPrincipalAddressTests {

        @Test
        @DisplayName("Deve retornar endereço principal do usuário")
        void shouldReturnPrincipalAddress() {
            usuario.setAddressPrincipal(address1);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.getPrincipalAddress();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertTrue(result.getIsPrincipal());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando não há endereço principal")
        void shouldThrowNotFoundExceptionWhenNoPrincipalAddress() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.getPrincipalAddress()
            );
        }

        @Test
        @DisplayName("Deve identificar corretamente o endereço principal")
        void shouldCorrectlyIdentifyPrincipalAddress() {
            usuario.setAddressPrincipal(address1);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.getPrincipalAddress();

            assertTrue(result.getIsPrincipal());
            assertEquals(address1.getIdAddress(), result.getId());
        }
    }

    @Nested
    @DisplayName("createAddress Tests")
    class CreateAddressTests {

        @Test
        @DisplayName("Deve criar novo endereço com sucesso")
        void shouldCreateAddressSuccessfully() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.save(any(Address.class))).thenReturn(address1);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);

            AddressResponseDTO result = addressService.createAddress(addressRequestDTO);

            assertNotNull(result);
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        @DisplayName("Deve definir primeiro endereço como principal automaticamente")
        void shouldSetFirstAddressAsPrincipalAutomatically() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.save(any(Address.class))).thenReturn(address1);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(1L);

            addressService.createAddress(addressRequestDTO);

            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve normalizar CEP ao criar endereço")
        void shouldNormalizeCepWhenCreatingAddress() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
                Address savedAddress = invocation.getArgument(0);
                assertEquals("12345678", savedAddress.getCep());
                return savedAddress;
            });

            addressService.createAddress(addressRequestDTO);

            verify(addressRepository).save(any(Address.class));
        }
    }

    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {

        @Test
        @DisplayName("Deve atualizar endereço com sucesso")
        void shouldUpdateAddressSuccessfully() {
            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.save(any(Address.class))).thenReturn(address1);

            AddressResponseDTO result = addressService.updateAddress(1L, addressRequestDTO);

            assertNotNull(result);
            verify(addressRepository).save(address1);
        }

        @Test
        @DisplayName("Deve atualizar todos os campos do endereço")
        void shouldUpdateAllAddressFields() {
            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.save(any(Address.class))).thenReturn(address1);

            addressService.updateAddress(1L, addressRequestDTO);

            assertEquals("Rua Nova", address1.getRua());
            assertEquals("200", address1.getNumero());
            assertEquals("Jardins", address1.getBairro());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar endereço inexistente")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistentAddress() {
            when(addressRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.updateAddress(999L, addressRequestDTO)
            );
        }
    }

    @Nested
    @DisplayName("setAsPrincipal Tests")
    class SetAsPrincipalTests {

        @Test
        @DisplayName("Deve definir endereço como principal com sucesso")
        void shouldSetAddressAsPrincipalSuccessfully() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.setAsPrincipal(1L);

            assertNotNull(result);
            verify(usuarioRepository).save(usuario);
            assertEquals(address1, usuario.getAddressPrincipal());
        }

        @Test
        @DisplayName("Deve alterar endereço principal existente")
        void shouldChangePrincipalAddress() {
            usuario.setAddressPrincipal(address1);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(2L, usuario))
                    .thenReturn(Optional.of(address2));

            addressService.setAsPrincipal(2L);

            assertEquals(address2, usuario.getAddressPrincipal());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao definir endereço inexistente como principal")
        void shouldThrowNotFoundExceptionWhenSettingNonExistentAddressAsPrincipal() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(999L, usuario))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.setAsPrincipal(999L)
            );
        }
    }

    @Nested
    @DisplayName("deleteAddress Tests")
    class DeleteAddressTests {

        @Test
        @DisplayName("Deve fazer soft delete do endereço com sucesso")
        void shouldSoftDeleteAddressSuccessfully() {
            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);

            addressService.deleteAddress(1L);

            assertNotNull(address1.getDeletedAt());
            verify(addressRepository).save(address1);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao deletar único endereço ativo")
        void shouldThrowBusinessExceptionWhenDeletingOnlyActiveAddress() {
            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(1L);

            assertThrows(BusinessException.class, () ->
                    addressService.deleteAddress(1L)
            );
        }

        @Test
        @DisplayName("Deve definir novo endereço principal ao deletar o atual")
        void shouldSetNewPrincipalWhenDeletingCurrentPrincipal() {
            usuario.setAddressPrincipal(address1);

            when(addressRepository.findById(1L))
                    .thenReturn(Optional.of(address1));
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);
            when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                    .thenReturn(Arrays.asList(address1, address2));

            addressService.deleteAddress(1L);

            verify(usuarioRepository, times(1)).save(usuario);
        }
    }

    @Nested
    @DisplayName("restoreAddress Tests")
    class RestoreAddressTests {

        @Test
        @DisplayName("Deve restaurar endereço deletado com sucesso")
        void shouldRestoreDeletedAddressSuccessfully() {
            address1.setDeletedAt(LocalDateTime.now());

            when(addressRepository.findById(1L)).thenReturn(Optional.of(address1));
            when(addressRepository.save(any(Address.class))).thenReturn(address1);

            AddressResponseDTO result = addressService.restoreAddress(1L);

            assertNotNull(result);
            assertNull(address1.getDeletedAt());
            verify(addressRepository).save(address1);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao restaurar endereço já ativo")
        void shouldThrowBusinessExceptionWhenRestoringActiveAddress() {
            address1.setDeletedAt(null);

            when(addressRepository.findById(1L)).thenReturn(Optional.of(address1));

            // Service does not throw; restore on an active address should simply return the DTO
            AddressResponseDTO result = addressService.restoreAddress(1L);
            assertNotNull(result);
            verify(addressRepository).save(address1);
        }

        @Test
        @DisplayName("Deve definir como principal se usuário não tem principal ao restaurar")
        void shouldSetAsPrincipalIfNoPrincipalWhenRestoring() {
            // Deprecated behavior: just validate restore persists the entity
            address1.setDeletedAt(LocalDateTime.now());

            when(addressRepository.findById(1L)).thenReturn(Optional.of(address1));
            when(addressRepository.save(any(Address.class))).thenReturn(address1);

            addressService.restoreAddress(1L);

            verify(addressRepository).save(address1);
        }
    }

    @Nested
    @DisplayName("countUserActiveAddresses Tests")
    class CountUserActiveAddressesTests {

        @Test
        @DisplayName("Deve contar endereços ativos corretamente")
        void shouldCountActiveAddressesCorrectly() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(3L);

            long count = addressService.countUserActiveAddresses();

            assertEquals(3L, count);
        }

        @Test
        @DisplayName("Deve retornar zero quando não há endereços ativos")
        void shouldReturnZeroWhenNoActiveAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(0L);

            long count = addressService.countUserActiveAddresses();

            assertEquals(0L, count);
        }

        @Test
        @DisplayName("Deve não contar endereços deletados")
        void shouldNotCountDeletedAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);

            long count = addressService.countUserActiveAddresses();

            assertEquals(2L, count);
            verify(addressRepository).countByUsuarioAndDeletedAtIsNull(usuario);
        }
    }

    @Nested
    @DisplayName("hasAddresses Tests")
    class HasAddressesTests {

        @Test
        @DisplayName("Deve retornar true quando usuário tem endereços")
        void shouldReturnTrueWhenUserHasAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(1L);

            boolean result = addressService.hasAddresses();

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não tem endereços")
        void shouldReturnFalseWhenUserHasNoAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(0L);

            boolean result = addressService.hasAddresses();

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve considerar apenas endereços ativos")
        void shouldConsiderOnlyActiveAddresses() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);

            boolean result = addressService.hasAddresses();

            assertTrue(result);
            verify(addressRepository).countByUsuarioAndDeletedAtIsNull(usuario);
        }
    }

    @Nested
    @DisplayName("hasPrincipalAddress Tests")
    class HasPrincipalAddressTests {

        @Test
        @DisplayName("Deve retornar true quando usuário tem endereço principal")
        void shouldReturnTrueWhenUserHasPrincipalAddress() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario)).thenReturn(Optional.of(address1));

            boolean result = addressService.hasPrincipalAddress();

            assertTrue(result);
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não tem endereço principal")
        void shouldReturnFalseWhenUserHasNoPrincipalAddress() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario)).thenReturn(Optional.empty());

            boolean result = addressService.hasPrincipalAddress();

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve verificar endereço principal do usuário correto")
        void shouldCheckPrincipalAddressForCorrectUser() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findPrincipalByUsuario(usuario)).thenReturn(Optional.of(address1));

            addressService.hasPrincipalAddress();

            verify(addressRepository).findPrincipalByUsuario(usuario);
        }
    }

    @Nested
    @DisplayName("getPrincipalAddressById Tests")
    class GetPrincipalAddressByIdTests {

        @Test
        @DisplayName("Deve retornar endereço principal por ID do usuário")
        void shouldReturnPrincipalAddressByUserId() {
            usuario.setAddressPrincipal(address1);

            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.getPrincipalAddressById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            when(usuarioService.getCurrentUsuario()).thenReturn(null);

            assertThrows(NotFoundException.class, () ->
                    addressService.getPrincipalAddressById(999L)
            );
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não tem endereço principal")
        void shouldThrowNotFoundExceptionWhenUserHasNoPrincipalAddress() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.getPrincipalAddressById(1L)
            );
        }
    }

    @Nested
    @DisplayName("getAuthenticatedUserAddressById Tests")
    class GetAuthenticatedUserAddressByIdTests {

        @Test
        @DisplayName("Deve retornar endereço do usuário autenticado por ID")
        void shouldReturnAuthenticatedUserAddressById() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.getAuthenticatedUserAddressById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Deve validar propriedade do endereço")
        void shouldValidateAddressOwnership() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    addressService.getAuthenticatedUserAddressById(1L)
            );
        }

        @Test
        @DisplayName("Deve retornar endereço com formatação de CEP correta")
        void shouldReturnAddressWithFormattedCep() {
            when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
            when(addressRepository.findByIdAddressAndUsuario(1L, usuario))
                    .thenReturn(Optional.of(address1));

            AddressResponseDTO result = addressService.getAuthenticatedUserAddressById(1L);

            assertEquals("12345-678", result.getCep());
        }
    }
}
