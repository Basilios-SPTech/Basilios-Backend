package com.basilios.basilios.app.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para retornar promoções ativas na Home (endpoint /promotions/current)
 * Sem relações complexas para evitar problemas de serialização
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCurrentDTO {

    private Long id;

    private String title;

    private String description;

    private BigDecimal discountPercentage;

    private BigDecimal discountAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;

    private Long productId;
}
