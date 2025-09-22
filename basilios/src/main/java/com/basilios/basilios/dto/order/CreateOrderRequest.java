package com.basilios.basilios.dto.order;

import com.basilios.basilios.dto.endereco.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotNull
    @Valid
    private EnderecoRequest enderecoEntrega;
}