package com.basilios.basilios.app.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "ID do endereço de entrega é obrigatório")
    private Long addressId;

    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid
    @Builder.Default
    private List<OrderItemRequest> items = new ArrayList<>();

    @DecimalMin(value = "0.00", message = "Taxa de entrega deve ser maior ou igual a zero")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.00", message = "Desconto deve ser maior ou igual a zero")
    private BigDecimal discount;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observations;

    /**
     * DTO para representar um item do pedido na requisição
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "ID do produto é obrigatório")
        private Long productId;

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        private Integer quantity;

        @Size(max = 500, message = "Observações do item devem ter no máximo 500 caracteres")
        private String observations;
    }
}