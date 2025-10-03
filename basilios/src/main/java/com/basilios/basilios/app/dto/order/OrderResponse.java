package com.basilios.basilios.app.dto.order;

import com.basilios.basilios.core.enums.StatusPedidoEnum;
import com.basilios.basilios.core.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private List<Order.OrderItem> items;
    private BigDecimal total;
    private StatusPedidoEnum status;
    private LocalDateTime createdAt;

    // Para redirecionamento quando fora da área de entrega
    private Boolean redirectToPartners;
    private Map<String, String> partnerLinks;
}