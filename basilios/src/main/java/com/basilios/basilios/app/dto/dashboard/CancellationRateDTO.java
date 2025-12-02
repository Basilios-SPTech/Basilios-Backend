package com.basilios.basilios.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationRateDTO {
    private double cancellationRate;

    public static CancellationRateDTO toResponse(double value) {
        return CancellationRateDTO.builder().cancellationRate(value).build();
    }
}
