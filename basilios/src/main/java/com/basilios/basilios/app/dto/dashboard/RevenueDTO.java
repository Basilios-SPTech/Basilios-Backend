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
public class RevenueDTO {
    private BigDecimal revenue;

    public static RevenueDTO toResponse(BigDecimal value) {
        return RevenueDTO.builder().revenue(value).build();
    }
}
