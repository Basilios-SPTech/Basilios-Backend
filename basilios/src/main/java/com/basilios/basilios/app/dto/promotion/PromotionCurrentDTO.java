package com.basilios.basilios.app.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private Boolean isActive;

    private Long productId;
}
