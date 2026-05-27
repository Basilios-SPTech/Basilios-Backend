package com.basilios.basilios.app.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePaymentStatusDTO {

    @NotBlank(message = "Status de pagamento é obrigatório")
    private String status;
}
