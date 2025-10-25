package com.basilios.basilios.app.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;

    private String name;

    private String description;

    @Builder.Default
    private List<IngredientResponse> ingredients = new ArrayList<>();

    private BigDecimal price;

    private BigDecimal finalPrice; // Com promoções aplicadas

    private Boolean isOnPromotion;

    private PromotionSummary currentPromotion;

    private Boolean isPaused;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * DTO para ingrediente na resposta
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientResponse {
        private Long id;
        private String name;
        private Integer quantity;
        private String measurementUnit;
    }

    /**
     * DTO para resumo de promoção
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionSummary {
        private Long id;
        private String title;
        private BigDecimal discountPercentage;
        private BigDecimal discountAmount;
        private BigDecimal savings; // Economia
    }
}