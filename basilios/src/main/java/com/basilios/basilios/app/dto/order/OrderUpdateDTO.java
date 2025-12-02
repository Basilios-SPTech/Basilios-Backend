package com.basilios.basilios.app.dto.order;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private StatusPedidoEnum status;

    @DecimalMin(value = "0.00", message = "Taxa de entrega deve ser maior ou igual a zero")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.00", message = "Desconto deve ser maior ou igual a zero")
    private BigDecimal discount;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observations;

    private Long addressId;

    @Size(max = 1000)
    private String motivo;
}

