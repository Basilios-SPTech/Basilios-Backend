package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.store.StoreHoursDayDTO;
import com.basilios.basilios.app.dto.store.StoreHoursResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Servico de Horarios de Funcionamento")
class BusinessHoursServiceTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private BusinessHoursService businessHoursService;

    @Test
    @DisplayName("Segunda-feira as 12:00 deve estar aberto")
    void testMondayAt12Open() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.MONDAY))
                .thenReturn(openDay(DayOfWeek.MONDAY, "09:00", "20:00"));

        LocalDateTime monday12 = LocalDateTime.of(2024, 5, 6, 12, 0);
        assertTrue(businessHoursService.isOpen(monday12));
    }

    @Test
    @DisplayName("Segunda-feira as 20:01 deve estar fechado")
    void testMondayAt2001Closed() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.MONDAY))
                .thenReturn(openDay(DayOfWeek.MONDAY, "09:00", "20:00"));

        LocalDateTime monday2001 = LocalDateTime.of(2024, 5, 6, 20, 1);
        assertFalse(businessHoursService.isOpen(monday2001));
    }

    @Test
    @DisplayName("Domingo fechado deve retornar false")
    void testSundayClosed() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.SUNDAY))
                .thenReturn(closedDay(DayOfWeek.SUNDAY));

        LocalDateTime sunday12 = LocalDateTime.of(2024, 5, 12, 12, 0);
        assertFalse(businessHoursService.isOpen(sunday12));
    }

    @Test
    @DisplayName("Validacao deve lancar excecao quando loja esta fechada")
    void testValidateClosedThrowsException() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.MONDAY))
                .thenReturn(openDay(DayOfWeek.MONDAY, "09:00", "20:00"));

        LocalDateTime monday2301 = LocalDateTime.of(2024, 5, 6, 23, 1);
        assertThrows(BusinessException.class, () -> businessHoursService.validateIsOpen(monday2301));
    }

    @Test
    @DisplayName("Validacao nao deve lancar excecao quando loja esta aberta")
    void testValidateOpenDoesNotThrow() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.MONDAY))
                .thenReturn(openDay(DayOfWeek.MONDAY, "09:00", "20:00"));

        LocalDateTime monday18 = LocalDateTime.of(2024, 5, 6, 18, 0);
        assertDoesNotThrow(() -> businessHoursService.validateIsOpen(monday18));
    }

    @Test
    @DisplayName("Mensagem de horario deve ser retornada corretamente")
    void testBusinessHoursInfoReturned() {
        StoreHoursResponseDTO response = new StoreHoursResponseDTO(Arrays.asList(
                openDay(DayOfWeek.MONDAY, "09:00", "20:00"),
                openDay(DayOfWeek.TUESDAY, "09:00", "20:00"),
                openDay(DayOfWeek.WEDNESDAY, "09:00", "20:00"),
                openDay(DayOfWeek.THURSDAY, "09:00", "20:00"),
                openDay(DayOfWeek.FRIDAY, "09:00", "22:00"),
                openDay(DayOfWeek.SATURDAY, "10:00", "22:00"),
                closedDay(DayOfWeek.SUNDAY)
        ));
        when(storeService.getMainStoreHours()).thenReturn(response);

        String info = businessHoursService.getBusinessHoursInfo();
        assertNotNull(info);
        assertTrue(info.contains("Segunda"));
        assertTrue(info.contains("09:00"));
        assertTrue(info.contains("Domingo"));
        assertTrue(info.contains("Fechado"));
    }

    @Test
    @DisplayName("Horario de fechamento para segunda-feira deve ser 20:00")
    void testClosingTimeMonday() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.MONDAY))
                .thenReturn(openDay(DayOfWeek.MONDAY, "09:00", "20:00"));

        LocalTime closingTime = businessHoursService.getClosingTime(DayOfWeek.MONDAY);
        assertEquals(LocalTime.of(20, 0), closingTime);
    }

    @Test
    @DisplayName("Horario de abertura para sexta-feira deve ser 09:00")
    void testOpeningTimeFriday() {
        when(storeService.getMainStoreHoursByDay(DayOfWeek.FRIDAY))
                .thenReturn(openDay(DayOfWeek.FRIDAY, "09:00", "22:00"));

        LocalTime openingTime = businessHoursService.getOpeningTime(DayOfWeek.FRIDAY);
        assertEquals(LocalTime.of(9, 0), openingTime);
    }

    private StoreHoursDayDTO openDay(DayOfWeek day, String opensAt, String closesAt) {
        StoreHoursDayDTO dto = new StoreHoursDayDTO();
        dto.setDayOfWeek(day);
        dto.setClosed(false);
        dto.setOpensAt(LocalTime.parse(opensAt));
        dto.setClosesAt(LocalTime.parse(closesAt));
        return dto;
    }

    private StoreHoursDayDTO closedDay(DayOfWeek day) {
        StoreHoursDayDTO dto = new StoreHoursDayDTO();
        dto.setDayOfWeek(day);
        dto.setClosed(true);
        dto.setOpensAt(null);
        dto.setClosesAt(null);
        return dto;
    }
}

