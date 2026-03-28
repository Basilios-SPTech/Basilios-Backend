package com.basilios.basilios.app.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {

    @NotBlank(message = "Status é obrigatório")
    private String status;
}
