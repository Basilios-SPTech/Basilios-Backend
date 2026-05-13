package com.basilios.basilios.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO para retornar informações sobre o horário de funcionamento da loja
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHoursResponseDTO {

    private boolean isOpen;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String message;
    private String businessHours;

    public static BusinessHoursResponseDTO open(LocalTime openingTime, LocalTime closingTime) {
        return BusinessHoursResponseDTO.builder()
                .isOpen(true)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .message("A Basilios está aberta! Você já pode fazer seu pedido.")
                .businessHours("""
                        Horário de Funcionamento:
                        - De Segunda a Quinta: 12:00 - 23:00
                        - Sexta e Sábado: 12:00 - 00:00 (Meia-noite)
                        - Domingo: 12:00 - 18:00
                        """)
                .build();
    }

    public static BusinessHoursResponseDTO closed(LocalTime openingTime, LocalTime closingTime) {
        return BusinessHoursResponseDTO.builder()
                .isOpen(false)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .message("A Basilios está fechada no momento. Confira nosso horário de funcionamento.")
                .businessHours("""
                        Horário de Funcionamento:
                        - De Segunda a Quinta: 12:00 - 23:00
                        - Sexta e Sábado: 12:00 - 00:00 (Meia-noite)
                        - Domingo: 12:00 - 18:00
                        """)
                .build();
    }
}

