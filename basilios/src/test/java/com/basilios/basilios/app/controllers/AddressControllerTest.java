package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.service.AddressService;
import com.basilios.basilios.core.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AddressControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AddressService addressService; // mock manual
    private OrderService orderService;     // mock manual

    private AddressRequestDTO addressRequest;
    private AddressResponseDTO addressResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Criar mocks
        addressService = mock(AddressService.class);
        orderService = mock(OrderService.class);

        // Instanciar controller com mocks
        AddressController controller = new AddressController(addressService, orderService);

        // Criar MockMvc standalone (não usa Spring context)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Criar DTOs para usar nos testes
        addressRequest = AddressRequestDTO.builder()
                .cep("01310-100")
                .rua("Avenida Paulista")
                .numero("1000")
                .complemento("Apto 101")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .latitude(-23.561414)
                .longitude(-46.656170)
                .build();

        addressResponse = AddressResponseDTO.builder()
                .id(1L)
                .cep("01310-100")
                .rua("Avenida Paulista")
                .numero("1000")
                .complemento("Apto 101")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .latitude(-23.561414)
                .longitude(-46.656170)
                .isPrincipal(false)
                .build();
    }

    // ========== TESTE POSITIVO: createAddress() ==========
    @Test
    @DisplayName("POST /address - Deve criar endereço com sucesso")
    void createAddress_DeveCriarEnderecoComSucesso() throws Exception {
        when(addressService.createAddress(any(AddressRequestDTO.class)))
                .thenReturn(addressResponse);

        mockMvc.perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cep", is("01310-100")))
                .andExpect(jsonPath("$.rua", is("Avenida Paulista")));

        verify(addressService, times(1)).createAddress(any(AddressRequestDTO.class));
    }

    // ========== TESTE NEGATIVO: createAddress() ==========
    @Test
    @DisplayName("POST /address - Deve retornar 400 quando dados inválidos")
    void createAddress_DeveRetornar400QuandoDadosInvalidos() throws Exception {
        AddressRequestDTO invalido = AddressRequestDTO.builder()
                .cep("")
                .rua("")
                .numero("")
                .bairro("")
                .cidade("")
                .estado("")
                .build();

        mockMvc.perform(post("/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(addressService, never()).createAddress(any());
    }

    // ========== TESTE POSITIVO: findById() ==========
    @Test
    @DisplayName("GET /address/{id} - Deve retornar endereço por ID")
    void findById_DeveRetornarEnderecoPorId() throws Exception {
        when(addressService.findById(1L)).thenReturn(addressResponse);

        mockMvc.perform(get("/address/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cep", is("01310-100")))
                .andExpect(jsonPath("$.cidade", is("São Paulo")));

        verify(addressService, times(1)).findById(1L);
    }

    // ========== TESTE NEGATIVO: findById() ==========
    @Test
    @DisplayName("GET /address/{id} - Deve retornar 404 quando endereço não existe")
    void findById_DeveRetornar404QuandoEnderecoNaoExiste() throws Exception {
        when(addressService.findById(999L))
                .thenThrow(new NotFoundException("Endereço não encontrado: 999"));

        mockMvc.perform(get("/address/999"))
                .andExpect(status().isNotFound());

        verify(addressService, times(1)).findById(999L);
    }
}
