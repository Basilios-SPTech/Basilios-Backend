package com.basilios.basilios.app.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para retornar Promotion após operaçõess (create, update, get)
 * Sem relações complexas para evitar problemas de serialização circular
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponseDTO {

    private Long id;

    private String title;

    private String description;

    private BigDecimal discountPercentage;

    private BigDecimal discountAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
