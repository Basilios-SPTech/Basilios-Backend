package com.basilios.basilios.app.dto.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePromotionDTO {

    private String title;
    private String description;

    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isActive;

    private List<Long> productIds;
}
