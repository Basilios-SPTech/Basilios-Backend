package com.basilios.basilios.app.dto.order;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSimpleDTO {
    private Long id;
    private StatusPedidoEnum status;
    private LocalDateTime createdAt;
    private BigDecimal total;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
}

