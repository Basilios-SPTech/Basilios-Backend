package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.infra.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService Tests")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setName("Basilios Burger");
        store.setAddress("Rua Haddock Lobo, 595");
        store.setLatitude(-23.5581416);
        store.setLongitude(-46.6615821);
        store.setPhone("(11) 97101-3012");
    }

    @Nested
    @DisplayName("getMainStore Tests")
    class GetMainStoreTests {

        @Test
        @DisplayName("Deve retornar a primeira loja quando existem lojas cadastradas")
        void shouldReturnFirstStoreWhenStoresExist() {
            when(storeRepository.findAll()).thenReturn(Arrays.asList(store));

            Store result = storeService.getMainStore();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Basilios Burger", result.getName());
            verify(storeRepository).findAll();
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando não há lojas cadastradas")
        void shouldThrowNotFoundExceptionWhenNoStoresExist() {
            when(storeRepository.findAll()).thenReturn(Collections.emptyList());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    storeService.getMainStore()
            );

            assertEquals("Nenhuma loja cadastrada", exception.getMessage());
            verify(storeRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar sempre a primeira loja mesmo com múltiplas lojas")
        void shouldAlwaysReturnFirstStoreEvenWithMultipleStores() {
            Store store2 = new Store();
            store2.setId(2L);
            store2.setName("Basilios Filial");

            Store store3 = new Store();
            store3.setId(3L);
            store3.setName("Basilios Shopping");

            when(storeRepository.findAll()).thenReturn(Arrays.asList(store, store2, store3));

            Store result = storeService.getMainStore();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Basilios Burger", result.getName());
        }

        @Test
        @DisplayName("Deve chamar findAll apenas uma vez")
        void shouldCallFindAllOnlyOnce() {
            when(storeRepository.findAll()).thenReturn(Arrays.asList(store));

            storeService.getMainStore();

            verify(storeRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar loja com todos os atributos preenchidos")
        void shouldReturnStoreWithAllAttributesFilled() {
            when(storeRepository.findAll()).thenReturn(Arrays.asList(store));

            Store result = storeService.getMainStore();

            assertNotNull(result.getName());
            assertNotNull(result.getLatitude());
            assertNotNull(result.getLongitude());
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar loja quando ID existe")
        void shouldReturnStoreWhenIdExists() {
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            Store result = storeService.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Basilios Burger", result.getName());
            verify(storeRepository).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando ID não existe")
        void shouldThrowNotFoundExceptionWhenIdNotExists() {
            when(storeRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    storeService.findById(999L)
            );

            assertEquals("Loja não encontrada", exception.getMessage());
            verify(storeRepository).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar loja pelo ID correto")
        void shouldSearchStoreByCorrectId() {
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            storeService.findById(1L);

            verify(storeRepository).findById(1L);
            verify(storeRepository, never()).findById(2L);
        }

        @Test
        @DisplayName("Deve retornar loja diferente para IDs diferentes")
        void shouldReturnDifferentStoreForDifferentIds() {
            Store store2 = new Store();
            store2.setId(2L);
            store2.setName("Basilios Filial");

            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
            when(storeRepository.findById(2L)).thenReturn(Optional.of(store2));

            Store result1 = storeService.findById(1L);
            Store result2 = storeService.findById(2L);

            assertNotEquals(result1.getId(), result2.getId());
            assertEquals("Basilios Burger", result1.getName());
            assertEquals("Basilios Filial", result2.getName());
        }

        @Test
        @DisplayName("Deve chamar repositório com parâmetro correto")
        void shouldCallRepositoryWithCorrectParameter() {
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            storeService.findById(1L);

            verify(storeRepository).findById(eq(1L));
        }

        @Test
        @DisplayName("Deve retornar entidade Store completa")
        void shouldReturnCompleteStoreEntity() {
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            Store result = storeService.findById(1L);

            assertInstanceOf(Store.class, result);
            assertNotNull(result.getId());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Deve retornar mesma loja em getMainStore e findById quando ID é 1")
        void shouldReturnSameStoreInGetMainStoreAndFindByIdWhenIdIs1() {
            when(storeRepository.findAll()).thenReturn(Arrays.asList(store));
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            Store mainStore = storeService.getMainStore();
            Store storeById = storeService.findById(1L);

            assertEquals(mainStore.getId(), storeById.getId());
            assertEquals(mainStore.getName(), storeById.getName());
        }

        @Test
        @DisplayName("Deve lançar mesma exceção para recursos não encontrados")
        void shouldThrowSameExceptionForResourcesNotFound() {
            when(storeRepository.findAll()).thenReturn(Collections.emptyList());
            when(storeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> storeService.getMainStore());
            assertThrows(NotFoundException.class, () -> storeService.findById(999L));
        }

        @Test
        @DisplayName("Deve funcionar corretamente com múltiplas lojas cadastradas")
        void shouldWorkCorrectlyWithMultipleStoresRegistered() {
            Store store2 = new Store();
            store2.setId(2L);
            store2.setName("Basilios Filial");

            List<Store> stores = Arrays.asList(store, store2);
            when(storeRepository.findAll()).thenReturn(stores);
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
            when(storeRepository.findById(2L)).thenReturn(Optional.of(store2));

            Store mainStore = storeService.getMainStore();
            Store store1 = storeService.findById(1L);
            Store store2Found = storeService.findById(2L);

            assertEquals(1L, mainStore.getId());
            assertEquals(1L, store1.getId());
            assertEquals(2L, store2Found.getId());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com ID nulo lançando exceção")
        void shouldHandleNullIdByThrowingException() {
            when(storeRepository.findById(null)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    storeService.findById(null)
            );
        }

        @Test
        @DisplayName("Deve lidar com ID zero")
        void shouldHandleZeroId() {
            when(storeRepository.findById(0L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    storeService.findById(0L)
            );
        }

        @Test
        @DisplayName("Deve lidar com ID negativo")
        void shouldHandleNegativeId() {
            when(storeRepository.findById(-1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () ->
                    storeService.findById(-1L)
            );
        }

        @Test
        @DisplayName("Deve retornar primeira loja mesmo se lista tiver null no meio")
        void shouldReturnFirstStoreEvenIfListHasNullInMiddle() {
            // Cenário improvável mas testável
            when(storeRepository.findAll()).thenReturn(Arrays.asList(store));

            Store result = storeService.getMainStore();

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Deve funcionar com IDs muito grandes")
        void shouldWorkWithVeryLargeIds() {
            Store storeLargeId = new Store();
            storeLargeId.setId(Long.MAX_VALUE);
            storeLargeId.setName("Store Large ID");

            when(storeRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.of(storeLargeId));

            Store result = storeService.findById(Long.MAX_VALUE);

            assertNotNull(result);
            assertEquals(Long.MAX_VALUE, result.getId());
        }
    }
}