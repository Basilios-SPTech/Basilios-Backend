package com.basilios.basilios.core.service;

import com.basilios.basilios.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Serviço responsável por validar os horários de funcionamento da loja
 * 
 * Regras de Funcionamento:
 * - Segunda a quinta-feira: 12:00 até 23:00
 * - Sexta-feira e sábado: 12:00 até 00:00 (meia-noite)
 * - Domingo: 12:00 até 18:00
 */
@Service
@Slf4j
public class BusinessHoursService {

    private static final LocalTime OPENING_TIME = LocalTime.of(12, 0);
    private static final LocalTime CLOSING_TIME_WEEKDAY = LocalTime.of(23, 0);
    private static final LocalTime CLOSING_TIME_WEEKEND = LocalTime.of(0, 0);
    private static final LocalTime CLOSING_TIME_SUNDAY = LocalTime.of(18, 0);

    /**
     * Verifica se a loja está aberta no horário atual
     * 
     * @return true se a loja está aberta, false caso contrário
     */
    public boolean isOpen() {
        return isOpen(LocalDateTime.now());
    }

    /**
     * Verifica se a loja está aberta em um horário específico
     * 
     * @param dateTime horário a verificar
     * @return true se a loja está aberta nesse horário, false caso contrário
     */
    public boolean isOpen(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        return switch (dayOfWeek) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> isTimeInRange(time, OPENING_TIME, CLOSING_TIME_WEEKDAY);
            case FRIDAY, SATURDAY -> isTimeInRange(time, OPENING_TIME, CLOSING_TIME_WEEKEND);
            case SUNDAY -> isTimeInRange(time, OPENING_TIME, CLOSING_TIME_SUNDAY);
        };
    }

    /**
     * Valida se a loja está aberta. Se estiver fechada, lança BusinessException
     * 
     * @throws BusinessException se a loja estiver fechada
     */
    public void validateIsOpen() {
        validateIsOpen(LocalDateTime.now());
    }

    /**
     * Valida se a loja está aberta em um horário específico. Se estiver fechada, lança BusinessException
     * 
     * @param dateTime horário a verificar
     * @throws BusinessException se a loja estiver fechada nesse horário
     */
    public void validateIsOpen(LocalDateTime dateTime) {
        if (!isOpen(dateTime)) {
            log.warn("Tentativa de criar pedido fora do horário de funcionamento. DateTime: {}", dateTime);
            throw new BusinessException("A Basilios está fechada no momento. Confira nosso horário de funcionamento.");
        }
    }

    /**
     * Retorna uma string com os horários de funcionamento da loja
     */
    public String getBusinessHoursInfo() {
        return """
                Horário de Funcionamento:
                - De Segunda a Quinta: 12:00 - 23:00
                - Sexta e Sábado: 12:00 - 00:00 (Meia-noite)
                - Domingo: 12:00 - 18:00
                """;
    }

    /**
     * Verifica se um horário está dentro de um intervalo (inclusive)
     * 
     * @param time horário a verificar
     * @param startTime horário de abertura
     * @param endTime horário de fechamento
     * @return true se o horário está no intervalo, false caso contrário
     */
    private boolean isTimeInRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
        // Se a hora de encerramento é meia-noite (00:00), significa que funciona até a meia-noite
        if (endTime.equals(LocalTime.MIDNIGHT)) {
            return time.isAfter(startTime) || time.equals(startTime) || time.equals(LocalTime.MIDNIGHT);
        }

        // Caso normal: hora atual >= hora de abertura E hora atual <= hora de fechamento
        return (time.isAfter(startTime) || time.equals(startTime)) && 
               (time.isBefore(endTime) || time.equals(endTime));
    }

    /**
     * Retorna o horário de fechamento para um dia específico
     */
    public LocalTime getClosingTime(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> CLOSING_TIME_WEEKDAY;
            case FRIDAY, SATURDAY -> CLOSING_TIME_WEEKEND;
            case SUNDAY -> CLOSING_TIME_SUNDAY;
        };
    }

    /**
     * Retorna o horário de abertura
     */
    public LocalTime getOpeningTime() {
        return OPENING_TIME;
    }
}

