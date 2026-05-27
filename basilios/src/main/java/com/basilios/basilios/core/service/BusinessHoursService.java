package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.store.StoreHoursDayDTO;
import com.basilios.basilios.app.dto.store.StoreHoursResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessHoursService {

    private final StoreService storeService;

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
        StoreHoursDayDTO dayHours = storeService.getMainStoreHoursByDay(dateTime.getDayOfWeek());
        if (dayHours.isClosed()) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        LocalTime opening = dayHours.getOpensAt();
        LocalTime closing = dayHours.getClosesAt();

        return (time.equals(opening) || time.isAfter(opening))
                && (time.equals(closing) || time.isBefore(closing));
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
            log.warn("Tentativa de criar pedido fora do horario de funcionamento. DateTime: {}", dateTime);
            throw new BusinessException("A Basilios esta fechada no momento. Confira nosso horario de funcionamento.");
        }
    }

    /**
     * Retorna uma string com os horários de funcionamento da loja
     */
    public String getBusinessHoursInfo() {
        StoreHoursResponseDTO hoursResponse = storeService.getMainStoreHours();
        Map<DayOfWeek, StoreHoursDayDTO> byDay = new EnumMap<>(DayOfWeek.class);
        for (StoreHoursDayDTO hour : hoursResponse.getHours()) {
            byDay.put(hour.getDayOfWeek(), hour);
        }

        return byDay.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getValue()))
                .map(entry -> formatDay(entry.getKey()) + ": " + formatWindow(entry.getValue()))
                .collect(Collectors.joining("\n", "Horario de Funcionamento:\n", ""));
    }

    /**
     * Retorna o horário de fechamento para um dia específico
     */
    public LocalTime getClosingTime(DayOfWeek dayOfWeek) {
        return storeService.getMainStoreHoursByDay(dayOfWeek).getClosesAt();
    }

    /**
     * Retorna o horário de abertura
     */
    public LocalTime getOpeningTime() {
        return getOpeningTime(LocalDateTime.now().getDayOfWeek());
    }

    /**
     * Retorna o horário de abertura para um dia específico
     */
    public LocalTime getOpeningTime(DayOfWeek dayOfWeek) {
        return storeService.getMainStoreHoursByDay(dayOfWeek).getOpensAt();
    }

    private String formatDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terca";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sabado";
            case SUNDAY -> "Domingo";
        };
    }

    private String formatWindow(StoreHoursDayDTO dto) {
        if (dto.isClosed()) {
            return "Fechado";
        }
        return dto.getOpensAt() + " - " + dto.getClosesAt();
    }
}
