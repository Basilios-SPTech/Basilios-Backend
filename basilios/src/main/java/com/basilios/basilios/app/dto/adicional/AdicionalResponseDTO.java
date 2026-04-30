package com.basilios.basilios.app.dto.adicional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionalResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String subcategory;
    private BigDecimal price;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
