package com.basilios.basilios.app.dto.promotion;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePromotionDTO {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
    private String title;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @DecimalMin(value = "0.00", message = "Percentual de desconto não pode ser negativo")
    @DecimalMax(value = "100.00", message = "Percentual de desconto não pode exceder 100%")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.00", message = "Valor de desconto não pode ser negativo")
    private BigDecimal discountAmount;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate startDate;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDate endDate;

    @NotEmpty(message = "Selecione ao menos um produto")
    private List<Long> productIds;

    @AssertTrue(message = "Data de fim deve ser posterior à data de início")
    private boolean isEndDateValid() {
        if (startDate == null || endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}