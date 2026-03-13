package com.basilios.basilios.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageDeliveryTimeDTO {
    private long averageSeconds;
    private String averageText;

    public static AverageDeliveryTimeDTO toResponse(long seconds, String text) {
        return AverageDeliveryTimeDTO.builder()
                .averageSeconds(seconds)
                .averageText(text)
                .build();
    }
}
