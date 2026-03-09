package com.basilios.basilios.core.service;

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
        when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                .thenReturn(List.of(address1));

        List<AddressResponseDTO> result = addressService.getUserAddresses();

        assertEquals(1, result.size());
        assertEquals("Rua A", result.get(0).getRua());
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(addressRepository, times(1))
                .findByUsuarioAndDeletedAtIsNull(usuario);
    }

    @Test
    @DisplayName("getUserAddresses() — Deve retornar lista vazia quando o usuário não tiver endereços")
    void getUserAddresses_DeveRetornarListaVazia_QuandoNenhumEnderecoExistir() {
        when(usuarioService.getCurrentUsuario()).thenReturn(usuario);
        when(addressRepository.findByUsuarioAndDeletedAtIsNull(usuario))
                .thenReturn(Collections.emptyList());

        List<AddressResponseDTO> result = addressService.getUserAddresses();

        assertTrue(result.isEmpty());
        verify(usuarioService, times(1)).getCurrentUsuario();
        verify(addressRepository, times(1))
                .findByUsuarioAndDeletedAtIsNull(usuario);
    }
}
