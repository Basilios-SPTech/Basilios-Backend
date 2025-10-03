package com.basilios.basilios.app.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuAvailabilityDTO {
    private List<Long> availableProducts;
    private List<UnavailableProduct> unavailableProducts;
    private LocalDateTime checkedAt;
    private Map<String, Object> summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnavailableProduct {
        private Long productId;
        private String productName;
        private String reason; // PAUSED, DELETED, OUT_OF_STOCK
        private LocalDateTime unavailableSince;
    }
}
