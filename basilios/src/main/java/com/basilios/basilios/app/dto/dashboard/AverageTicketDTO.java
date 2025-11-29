package com.basilios.basilios.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageTicketDTO {
    private BigDecimal averageTicket;

    public static AverageTicketDTO toResponse(BigDecimal value) {
        return AverageTicketDTO.builder().averageTicket(value).build();
    }
}
