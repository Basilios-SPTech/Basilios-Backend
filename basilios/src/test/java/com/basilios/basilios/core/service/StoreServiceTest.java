package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.infra.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StoreService")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private Store store;

    @BeforeEach
    void setUp() {
        // Criar loja mock
        store = new Store();
        store.setId(1L);
        store.setName("Basilios Pizzaria");
        store.setLatitude(-23.550520);
        store.setLongitude(-46.633308);
        store.setPhone("11987654321");
    }

    // ========== CENÁRIO POSITIVO ==========

    @Test
    @DisplayName("Deve retornar a loja principal quando existe loja cadastrada")
    void getMainStore_DeveRetornarLojaPrincipalComSucesso() {
        // Arrange
        List<Store> stores = List.of(store);
        when(storeRepository.findAll()).thenReturn(stores);

        // Act
        Store result = storeService.getMainStore();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Basilios Pizzaria", result.getName());
        assertEquals(-23.550520, result.getLatitude());
        assertEquals(-46.633308, result.getLongitude());
        verify(storeRepository, times(1)).findAll();
    }

    // ========== CENÁRIO NEGATIVO ==========

    @Test
    @DisplayName("Deve lançar NotFoundException quando não há lojas cadastradas")
    void getMainStore_DeveLancarExcecaoQuandoNaoHaLojasCadastradas() {
        // Arrange
        List<Store> storesVazia = new ArrayList<>();
        when(storeRepository.findAll()).thenReturn(storesVazia);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> storeService.getMainStore());

        assertEquals("Nenhuma loja cadastrada", exception.getMessage());
        verify(storeRepository, times(1)).findAll();
    }
}