package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do Serviço de Horários de Funcionamento")
class BusinessHoursServiceTest {

    private BusinessHoursService businessHoursService;

    @BeforeEach
    void setup() {
        businessHoursService = new BusinessHoursService();
    }

    // ========== TESTES SEGUNDA A QUINTA ==========

    @Test
    @DisplayName("Segunda-feira às 12:00 deve estar aberto")
    void testMondayAt12Open() {
        LocalDateTime monday12 = LocalDateTime.of(2024, 5, 6, 12, 0); // Segunda
        assertTrue(businessHoursService.isOpen(monday12));
    }

    @Test
    @DisplayName("Segunda-feira às 18:00 deve estar aberto")
    void testMondayAt18Open() {
        LocalDateTime monday18 = LocalDateTime.of(2024, 5, 6, 18, 0); // Segunda
        assertTrue(businessHoursService.isOpen(monday18));
    }

    @Test
    @DisplayName("Segunda-feira às 23:00 deve estar aberto")
    void testMondayAt23Open() {
        LocalDateTime monday23 = LocalDateTime.of(2024, 5, 6, 23, 0); // Segunda
        assertTrue(businessHoursService.isOpen(monday23));
    }

    @Test
    @DisplayName("segunda-feira às 23:01 deve estar fechado")
    void testMondayAt2301Closed() {
        LocalDateTime monday2301 = LocalDateTime.of(2024, 5, 6, 23, 1); // Segunda
        assertFalse(businessHoursService.isOpen(monday2301));
    }

    @Test
    @DisplayName("Segunda-feira às 11:59 deve estar fechado")
    void testMondayAt1159Closed() {
        LocalDateTime monday1159 = LocalDateTime.of(2024, 5, 6, 11, 59); // Segunda
        assertFalse(businessHoursService.isOpen(monday1159));
    }

    // ========== TESTES SEXTA-FEIRA E SÁBADO ==========

    @Test
    @DisplayName("Sexta-feira às 23:59 deve estar aberto")
    void testFridayAt2359Open() {
        LocalDateTime friday2359 = LocalDateTime.of(2024, 5, 10, 23, 59); // Sexta
        assertTrue(businessHoursService.isOpen(friday2359));
    }

    @Test
    @DisplayName("Sexta-feira à meia-noite (00:00) deve estar aberto")
    void testFridayAtMidnightOpen() {
        LocalDateTime fridayMidnight = LocalDateTime.of(2024, 5, 11, 0, 0); // Sábado (meia-noite da sexta)
        assertTrue(businessHoursService.isOpen(fridayMidnight));
    }

    @Test
    @DisplayName("Sexta-feira às 00:01 deve estar fechado")
    void testFridayAt0001Closed() {
        LocalDateTime friday0001 = LocalDateTime.of(2024, 5, 11, 0, 1); // Sábado (00:01)
        assertFalse(businessHoursService.isOpen(friday0001));
    }

    @Test
    @DisplayName("Sábado às 23:00 deve estar aberto")
    void testSaturdayAt23Open() {
        LocalDateTime saturday23 = LocalDateTime.of(2024, 5, 11, 23, 0); // Sábado
        assertTrue(businessHoursService.isOpen(saturday23));
    }

    // ========== TESTES DOMINGO ==========

    @Test
    @DisplayName("Domingo às 12:00 deve estar aberto")
    void testSundayAt12Open() {
        LocalDateTime sunday12 = LocalDateTime.of(2024, 5, 12, 12, 0); // Domingo
        assertTrue(businessHoursService.isOpen(sunday12));
    }

    @Test
    @DisplayName("Domingo às 18:00 deve estar aberto")
    void testSundayAt18Open() {
        LocalDateTime sunday18 = LocalDateTime.of(2024, 5, 12, 18, 0); // Domingo
        assertTrue(businessHoursService.isOpen(sunday18));
    }

    @Test
    @DisplayName("Domingo às 18:01 deve estar fechado")
    void testSundayAt1801Closed() {
        LocalDateTime sunday1801 = LocalDateTime.of(2024, 5, 12, 18, 1); // Domingo
        assertFalse(businessHoursService.isOpen(sunday1801));
    }

    @Test
    @DisplayName("Domingo às 11:00 deve estar fechado")
    void testSundayAt11Closed() {
        LocalDateTime sunday11 = LocalDateTime.of(2024, 5, 12, 11, 0); // Domingo
        assertFalse(businessHoursService.isOpen(sunday11));
    }

    // ========== TESTES DE VALIDAÇÃO ==========

    @Test
    @DisplayName("Validacao deve lançar exceção quando loja está fechada")
    void testValidateClosedThrowsException() {
        LocalDateTime monday2301 = LocalDateTime.of(2024, 5, 6, 23, 1); // Segunda, fechado
        assertThrows(BusinessException.class, () -> businessHoursService.validateIsOpen(monday2301));
    }

    @Test
    @DisplayName("Validacao não deve lançar exceção quando loja está aberta")
    void testValidateOpenDoesNotThrow() {
        LocalDateTime monday18 = LocalDateTime.of(2024, 5, 6, 18, 0); // Segunda, aberto
        assertDoesNotThrow(() -> businessHoursService.validateIsOpen(monday18));
    }

    // ========== TESTES DE MENSAGENS ==========

    @Test
    @DisplayName("Mensagem de horário deve ser retornada corretamente")
    void testBusinessHoursInfoReturned() {
        String info = businessHoursService.getBusinessHoursInfo();
        assertNotNull(info);
        assertFalse(info.isEmpty());
        assertTrue(info.contains("Segunda"));
        assertTrue(info.contains("12:00"));
    }

    // ========== TESTES DE HORÁRIO DE FECHAMENTO ==========

    @Test
    @DisplayName("Horário de fechamento para segunda-feira deve ser 23:00")
    void testClosingTimeMonday() {
        LocalTime closingTime = businessHoursService.getClosingTime(DayOfWeek.MONDAY);
        assertEquals(LocalTime.of(23, 0), closingTime);
    }

    @Test
    @DisplayName("Horário de fechamento para sexta-feira deve ser 00:00")
    void testClosingTimeFriday() {
        LocalTime closingTime = businessHoursService.getClosingTime(DayOfWeek.FRIDAY);
        assertEquals(LocalTime.of(0, 0), closingTime);
    }

    @Test
    @DisplayName("Horário de fechamento para domingo deve ser 18:00")
    void testClosingTimeSunday() {
        LocalTime closingTime = businessHoursService.getClosingTime(DayOfWeek.SUNDAY);
        assertEquals(LocalTime.of(18, 0), closingTime);
    }

    @Test
    @DisplayName("Horário de abertura deve ser 12:00")
    void testOpeningTime() {
        LocalTime openingTime = businessHoursService.getOpeningTime();
        assertEquals(LocalTime.of(12, 0), openingTime);
    }
}

