package com.basilios.basilios.app.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusDTO {

    @NotNull(message = "Status de pausa é obrigatório")
    private Boolean isPaused;
}
