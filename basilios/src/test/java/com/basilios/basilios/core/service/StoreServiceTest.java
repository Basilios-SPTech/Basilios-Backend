package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.store.StoreHoursDayDTO;
import com.basilios.basilios.app.dto.store.StoreHoursResponseDTO;
import com.basilios.basilios.app.dto.store.StoreHoursUpdateDTO;
import com.basilios.basilios.app.dto.store.StorePatchUpdateDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Store;
import com.basilios.basilios.core.model.StoreOperatingHour;
import com.basilios.basilios.infra.repository.StoreOperatingHourRepository;
import com.basilios.basilios.infra.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StoreService")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreOperatingHourRepository storeOperatingHourRepository;

    @InjectMocks
    private StoreService storeService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setName("Basilios Pizzaria");
        store.setLatitude(-23.550520);
        store.setLongitude(-46.633308);
        store.setPhone("11987654321");
    }

    @Test
    @DisplayName("Deve retornar a loja principal quando existe loja cadastrada")
    void getMainStore_DeveRetornarLojaPrincipalComSucesso() {
        when(storeRepository.findAll()).thenReturn(List.of(store));

        Store result = storeService.getMainStore();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Basilios Pizzaria", result.getName());
        assertEquals(-23.550520, result.getLatitude());
        assertEquals(-46.633308, result.getLongitude());
        verify(storeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando não há lojas cadastradas")
    void getMainStore_DeveLancarExcecaoQuandoNaoHaLojasCadastradas() {
        when(storeRepository.findAll()).thenReturn(new ArrayList<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> storeService.getMainStore());

        assertEquals("Nenhuma loja cadastrada", exception.getMessage());
        verify(storeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar parcialmente a loja principal")
    void patchMainStore_DeveAtualizarCamposInformados() {
        store.setAddress("Rua Antiga");
        store.setDeliveryFee(new BigDecimal("5.00"));
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StorePatchUpdateDTO patch = new StorePatchUpdateDTO();
        patch.setName("Basilios Burger");
        patch.setDeliveryFee(new BigDecimal("9.90"));

        Store result = storeService.patchMainStore(patch);

        assertEquals("Basilios Burger", result.getName());
        assertEquals("Rua Antiga", result.getAddress());
        assertEquals(new BigDecimal("9.90"), result.getDeliveryFee());
        verify(storeRepository, times(1)).save(store);
    }

    @Test
    @DisplayName("Deve lancar excecao quando nome vier em branco no patch")
    void patchMainStore_DeveLancarExcecaoQuandoNomeBranco() {
        when(storeRepository.findAll()).thenReturn(List.of(store));

        StorePatchUpdateDTO patch = new StorePatchUpdateDTO();
        patch.setName("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storeService.patchMainStore(patch));

        assertEquals("Nome nao pode ser vazio", exception.getMessage());
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    @DisplayName("Deve atualizar e retornar horarios semanais da loja")
    void updateMainStoreHours_DevePersistirHorarios() {
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeOperatingHourRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        StoreHoursDayDTO monday = new StoreHoursDayDTO();
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        monday.setClosed(false);
        monday.setOpensAt(LocalTime.of(9, 0));
        monday.setClosesAt(LocalTime.of(20, 0));

        StoreHoursUpdateDTO request = new StoreHoursUpdateDTO();
        request.setHours(List.of(monday));

        StoreHoursResponseDTO result = storeService.updateMainStoreHours(request);

        assertNotNull(result);
        assertEquals(7, result.getHours().size());
        assertFalse(result.getHours().stream()
                .filter(hour -> hour.getDayOfWeek() == DayOfWeek.MONDAY)
                .findFirst()
                .orElseThrow()
                .isClosed());

        InOrder inOrder = inOrder(storeOperatingHourRepository);
        inOrder.verify(storeOperatingHourRepository).deleteAllByStoreIdInBulk(1L);
        inOrder.verify(storeOperatingHourRepository).flush();
        inOrder.verify(storeOperatingHourRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve retornar horarios fechados quando nao houver configuracao")
    void getMainStoreHours_DeveRetornarSemanaFechadaSemConfiguracao() {
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeOperatingHourRepository.findByStoreId(1L)).thenReturn(List.of());

        StoreHoursResponseDTO result = storeService.getMainStoreHours();

        assertEquals(7, result.getHours().size());
        assertTrue(result.getHours().stream().allMatch(StoreHoursDayDTO::isClosed));
    }

    @Test
    @DisplayName("Deve lancar excecao quando payload tiver dia da semana duplicado")
    void updateMainStoreHours_DeveLancarExcecaoComDiaDuplicado() {
        when(storeRepository.findAll()).thenReturn(List.of(store));

        StoreHoursDayDTO monday1 = new StoreHoursDayDTO();
        monday1.setDayOfWeek(DayOfWeek.MONDAY);
        monday1.setClosed(true);

        StoreHoursDayDTO monday2 = new StoreHoursDayDTO();
        monday2.setDayOfWeek(DayOfWeek.MONDAY);
        monday2.setClosed(true);

        StoreHoursUpdateDTO request = new StoreHoursUpdateDTO();
        request.setHours(List.of(monday1, monday2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storeService.updateMainStoreHours(request));

        assertTrue(exception.getMessage().contains("Dia da semana duplicado"));
        verify(storeOperatingHourRepository, never()).saveAll(anyList());
    }
}