package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServicePartialTest {

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

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);

        address1 = new Address();
        address1.setIdAddress(10L);
        address1.setUsuario(usuario);
        address1.setRua("Rua A");
        address1.setCep("12345678");

        address2 = new Address();
        address2.setIdAddress(20L);
        address2.setUsuario(usuario);
        address2.setRua("Rua B");
        address2.setCep("87654321");
    }

    // ============================================================
    //  TESTES DE findAllAddress()
    // ============================================================

    @Test
    @DisplayName("findAllAddress() — Deve retornar a lista completa de endereços")
    void findAllAddress_DeveRetornarListaDeEnderecos() {
        when(addressRepository.findAll()).thenReturn(List.of(address1, address2));

        List<AddressResponseDTO> result = addressService.findAllAddress();

        assertEquals(2, result.size());
        assertEquals("Rua A", result.get(0).getRua());
        assertEquals("Rua B", result.get(1).getRua());
        verify(addressRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllAddress() — Deve retornar lista vazia quando não existirem endereços")
    void findAllAddress_DeveRetornarListaVazia_QuandoNaoExistirEnderecos() {
        when(addressRepository.findAll()).thenReturn(Collections.emptyList());

        List<AddressResponseDTO> result = addressService.findAllAddress();

        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findAll();
    }

    // ============================================================
    //  TESTES DE findActiveAddressesByUserId()
    // ============================================================

    @Test
    @DisplayName("findActiveAddressesByUserId() — Deve retornar apenas endereços ativos do usuário")
    void findActiveAddressesByUserId_DeveRetornarEnderecosAtivos() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                .thenReturn(List.of(address1, address2));

        List<Address> result = addressService.findActiveAddressesByUserId(1L);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getIdAddress());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(addressRepository, times(1))
                .findByUsuarioAndDeletedAtIsNull(usuario);
    }

    @Test
    @DisplayName("findActiveAddressesByUserId() — Deve lançar NotFoundException quando o usuário não existir")
    void findActiveAddressesByUserId_DeveLancarException_QuandoUsuarioNaoExistir() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            addressService.findActiveAddressesByUserId(999L);
        });

        verify(usuarioRepository, times(1)).findById(999L);
    }

    // ============================================================
    //  TESTES DE getUserAddresses()
    // ============================================================

    @Test
    @DisplayName("getUserAddresses() — Deve retornar os endereços do usuário autenticado")
    void getUserAddresses_DeveRetornarEnderecosDoUsuarioAutenticado() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findByUsuarioAndDeletedAtIsNullOrderByCreatedAtDescIdAddressDesc(usuario))
                .thenReturn(List.of(address1));

        List<AddressResponseDTO> result = addressService.getUserAddresses();

        assertEquals(1, result.size());
        assertEquals("Rua A", result.get(0).getRua());
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(addressRepository, times(1))
                .findByUsuarioAndDeletedAtIsNullOrderByCreatedAtDescIdAddressDesc(usuario);
    }

    @Test
    @DisplayName("getUserAddresses() — Deve retornar lista vazia quando o usuário não tiver endereços")
    void getUserAddresses_DeveRetornarListaVazia_QuandoNenhumEnderecoExistir() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findByUsuarioAndDeletedAtIsNullOrderByCreatedAtDescIdAddressDesc(usuario))
                .thenReturn(Collections.emptyList());

        List<AddressResponseDTO> result = addressService.getUserAddresses();

        assertTrue(result.isEmpty());
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(addressRepository, times(1))
                .findByUsuarioAndDeletedAtIsNullOrderByCreatedAtDescIdAddressDesc(usuario);
    }

    @Test
    @DisplayName("findById() — Deve retornar endereço quando pertence ao usuário autenticado")
    void findById_DeveRetornarEndereco_QuandoEncontrado() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        AddressResponseDTO result = addressService.findById(10L);

        assertNotNull(result);
        assertEquals("Rua A", result.getRua());
        verify(addressRepository).findById(10L);
    }

    @Test
    @DisplayName("findById() — Deve lançar AccessDeniedException quando endereço for de outro usuário")
    void findById_DeveLancarAccessDenied_QuandoEnderecoForDeOutroUsuario() {
        Usuario outro = new Usuario();
        outro.setId(77L);
        address1.setUsuario(outro);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        assertThrows(AccessDeniedException.class, () -> addressService.findById(10L));
    }

    // ============================================================
    //  TESTES DE createAddress()
    // ============================================================

    @Test
    @DisplayName("createAddress() — Deve criar endereço com sucesso")
    void createAddress_DeveCriarEnderecoComSucesso() {
        AddressRequestDTO request = AddressRequestDTO.builder()
                .rua("Rua Nova").numero("100").bairro("Centro")
                .cep("01234-567").cidade("São Paulo").estado("SP")
                .latitude(-23.5).longitude(-46.6).build();

        Address saved = new Address();
        saved.setIdAddress(30L);
        saved.setUsuario(usuario);
        saved.setRua("Rua Nova");
        saved.setCep("01234567");

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.save(any(Address.class))).thenReturn(saved);

        AddressResponseDTO result = addressService.createAddress(request);

        assertNotNull(result);
        assertEquals("Rua Nova", result.getRua());
        verify(addressRepository).save(any(Address.class));
    }

    // ============================================================
    //  TESTES DE updateAddress()
    // ============================================================

    @Test
    @DisplayName("updateAddress() — Deve atualizar endereço existente")
    void updateAddress_DeveAtualizarEnderecoExistente() {
        AddressRequestDTO request = AddressRequestDTO.builder()
                .rua("Rua Atualizada").numero("200").bairro("Novo Bairro")
                .cep("99999999").cidade("Rio").estado("RJ")
                .latitude(-22.9).longitude(-43.2).build();

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));
        when(addressRepository.save(any(Address.class))).thenReturn(address1);

        AddressResponseDTO result = addressService.updateAddress(10L, request);

        assertNotNull(result);
        verify(addressRepository).save(address1);
    }

    @Test
    @DisplayName("updateAddress() — Deve lançar AccessDeniedException quando endereço for de outro usuário")
    void updateAddress_DeveLancarAccessDenied_QuandoEnderecoNaoPertenceAoUsuario() {
        AddressRequestDTO request = AddressRequestDTO.builder()
                .rua("R").numero("1").bairro("B")
                .cep("00000000").cidade("C").estado("SP")
                .latitude(0.0).longitude(0.0).build();

        Usuario outro = new Usuario();
        outro.setId(99L);
        address1.setUsuario(outro);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        assertThrows(AccessDeniedException.class, () -> addressService.updateAddress(10L, request));
    }

    // ============================================================
    //  TESTES DE deleteAddress()
    // ============================================================

    @Test
    @DisplayName("deleteAddress() — Deve fazer soft delete quando endereço pertence ao usuário")
    void deleteAddress_DeveFazerSoftDelete() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        addressService.deleteAddress(10L);

        assertNotNull(address1.getDeletedAt());
        verify(addressRepository).save(address1);
    }

    @Test
    @DisplayName("deleteAddress() — Deve lançar AccessDeniedException quando endereço for de outro usuário")
    void deleteAddress_DeveLancarAccessDenied_QuandoEnderecoNaoPertenceAoUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(99L);
        address1.setUsuario(outroUsuario);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        assertThrows(AccessDeniedException.class, () -> addressService.deleteAddress(10L));
        verify(addressRepository, never()).save(any());
    }

    // ============================================================
    //  TESTES DE restoreAddress()
    // ============================================================

    @Test
    @DisplayName("restoreAddress() — Deve restaurar endereço deletado")
    void restoreAddress_DeveRestaurarEnderecoDeletado() {
        address1.setDeletedAt(java.time.LocalDateTime.now());
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));
        when(addressRepository.save(address1)).thenReturn(address1);

        AddressResponseDTO result = addressService.restoreAddress(10L);

        assertNotNull(result);
        assertNull(address1.getDeletedAt());
        verify(addressRepository).save(address1);
    }

    // ============================================================
    //  TESTES DE setAsPrincipal()
    // ============================================================

    @Test
    @DisplayName("setAsPrincipal() — Deve definir endereço como principal")
    void setAsPrincipal_DeveDefinirEnderecoPrincipal() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findByIdAddressAndUsuario(10L, usuario))
                .thenReturn(Optional.of(address1));

        AddressResponseDTO result = addressService.setAsPrincipal(10L);

        assertNotNull(result);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("setAsPrincipal() — Deve lançar NotFoundException quando endereço não pertence ao usuário")
    void setAsPrincipal_DeveLancarException_QuandoEnderecoNaoPertenceAoUsuario() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findByIdAddressAndUsuario(999L, usuario))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.setAsPrincipal(999L));
    }

    // ============================================================
    //  TESTES DE isOwner()
    // ============================================================

    @Test
    @DisplayName("isOwner() — Deve retornar true quando endereço pertence ao usuário")
    void isOwner_DeveRetornarTrue_QuandoEnderecoPertenceAoUsuario() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        assertTrue(addressService.isOwner(10L));
    }

    @Test
    @DisplayName("isOwner() — Deve retornar false quando endereço não pertence ao usuário")
    void isOwner_DeveRetornarFalse_QuandoEnderecoNaoPertenceAoUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(99L);
        address1.setUsuario(outroUsuario);

        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address1));

        assertFalse(addressService.isOwner(10L));
    }

    // ============================================================
    //  TESTES DE countUserActiveAddresses() e hasAddresses()
    // ============================================================

    @Test
    @DisplayName("countUserActiveAddresses() — Deve retornar contagem de endereços ativos")
    void countUserActiveAddresses_DeveRetornarContagem() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(3L);

        assertEquals(3L, addressService.countUserActiveAddresses());
    }

    @Test
    @DisplayName("hasAddresses() — Deve retornar true quando há endereços")
    void hasAddresses_DeveRetornarTrue_QuandoHaEnderecos() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(2L);

        assertTrue(addressService.hasAddresses());
    }

    @Test
    @DisplayName("hasAddresses() — Deve retornar false quando não há endereços")
    void hasAddresses_DeveRetornarFalse_QuandoNaoHaEnderecos() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.countByUsuarioAndDeletedAtIsNull(usuario)).thenReturn(0L);

        assertFalse(addressService.hasAddresses());
    }

    // ============================================================
    //  TESTES DE hasPrincipalAddress()
    // ============================================================

    @Test
    @DisplayName("hasPrincipalAddress() — Deve retornar true quando há endereço principal")
    void hasPrincipalAddress_DeveRetornarTrue() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findPrincipalByUsuario(usuario)).thenReturn(Optional.of(address1));

        assertTrue(addressService.hasPrincipalAddress());
    }

    @Test
    @DisplayName("hasPrincipalAddress() — Deve retornar false quando não há endereço principal")
    void hasPrincipalAddress_DeveRetornarFalse() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findPrincipalByUsuario(usuario)).thenReturn(Optional.empty());

        assertFalse(addressService.hasPrincipalAddress());
    }
}
